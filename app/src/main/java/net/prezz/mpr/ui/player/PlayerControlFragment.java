package net.prezz.mpr.ui.player;

import java.lang.ref.WeakReference;

import net.prezz.mpr.Utils;
import net.prezz.mpr.model.LibraryEntity;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.command.ConsumeCommand;
import net.prezz.mpr.model.command.NextCommand;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PreviousCommand;
import net.prezz.mpr.model.command.RandomCommand;
import net.prezz.mpr.model.command.RepeatCommand;
import net.prezz.mpr.model.command.SeekCommand;
import net.prezz.mpr.model.command.StopCommand;
import net.prezz.mpr.model.command.VolumeDownCommand;
import net.prezz.mpr.model.command.VolumeUpCommand;
import net.prezz.mpr.model.external.CoverReceiver;
import net.prezz.mpr.model.external.ExternalInformationService;
import net.prezz.mpr.model.external.UrlReceiver;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.ToggleButtonHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.library.filtered.FilteredActivity;
import net.prezz.mpr.ui.library.filtered.FilteredAlbumAndTitleActivity;
import net.prezz.mpr.ui.library.filtered.FilteredTrackAndTitleActivity;
import net.prezz.mpr.R;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerControlFragment extends Fragment implements PlayerFragment, OnClickListener {

    public static final int FRAGMENT_POSITION = 1;

    private static final String PREFERENCE_SEEK_BAR_ENABLED_KEY = "seek_bar_enabled_key";

    private UpdateTimeHandler updateTimeHandler;
    private PlayerStatus playerStatus = new PlayerStatus(false);
    private PlaylistEntity[] playlistEntities;
    private boolean seeking = false;

    private TaskHandle getCoverHandle = TaskHandle.NULL_HANDLE;
    private TaskHandle lastFmHandle = TaskHandle.NULL_HANDLE;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_control, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SeekBar seekBar = findSeekBar();
        setSeekBarEnablement(seekBar);
        seekBar.setOnSeekBarChangeListener(new TimeBarChangedListener());

        updateTimeHandler = new UpdateTimeHandler(this);

        ((PlayerActivity)getActivity()).attachFragment(this, FRAGMENT_POSITION);

        setupButtonClickListener(view, R.id.player_button_volume_down);
        setupButtonClickListener(view, R.id.player_button_volume_up);
        setupButtonClickListener(view, R.id.player_button_repeat);
        setupButtonClickListener(view, R.id.player_button_consume);
        setupButtonClickListener(view, R.id.player_button_random);
        setupButtonClickListener(view, R.id.player_button_previous);
        setupButtonClickListener(view, R.id.player_button_stop);
        setupButtonClickListener(view, R.id.player_button_play);
        setupButtonClickListener(view, R.id.player_button_next);
    }

    @Override
    public void onPause() {
        updateTimeHandler.running(false);
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        getCoverHandle.cancelTask();
        lastFmHandle.cancelTask();
        ((PlayerActivity)getActivity()).detachFragment(FRAGMENT_POSITION);
        super.onDestroyView();
    }

    @Override
    public void statusUpdated(PlayerStatus status) {
        refreshTime(status.getElapsedTime(), status.getTotalTime());
        updateTimeHandler.running(status.getState() == PlayerState.PLAY);

        if (playerStatus.getVolume() != status.getVolume()) {
            setVolumeText(status.getVolume());
        }

        if (playerStatus.getRepeat() != status.getRepeat()) {
            toggleButton(R.id.player_button_repeat, status.getRepeat());
        }

        if (playerStatus.getConsume() != status.getConsume()) {
            toggleButton(R.id.player_button_consume, status.getConsume());
        }

        if (playerStatus.getRandom() != status.getRandom()) {
            toggleButton(R.id.player_button_random, status.getRandom());
        }

        if (playerStatus.getState() != status.getState()) {
            setPlayButtonState(status.getState());
        }

        boolean refreshPlaying = playerStatus.getPlaylistVersion() == status.getPlaylistVersion() && (playerStatus.getCurrentSong() != status.getCurrentSong());
        playerStatus = status;
        if (refreshPlaying) {
            refreshPlayingInfo();
        }
    }

    @Override
    public void playlistUpdated(PlaylistEntity[] playlistEntities) {
        this.playlistEntities = playlistEntities;
        refreshPlayingInfo();
    }

    @Override
    public void onChoiceMenuClick(View view) {
        final String[] items = getResources().getStringArray(R.array.player_control_choice_menu);
        SeekBar seekBar = findSeekBar();
        items[0] = String.format(items[0], getString(seekBar.isEnabled() ? R.string.player_seek_bar_disable : R.string.player_seek_bar_enable));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.player_remote_control));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
                        toggleSeekBarEnablement();
                        break;
                    case 1:
                        showNext();
                        break;
                    case 2:
                        goTo();
                        break;
                    case 3:
                        goToLastFm();
                        break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.player_button_volume_down:
                MusicPlayerControl.sendControlCommand(new VolumeDownCommand(VolumeButtonsHelper.getVolumeAmount(this.getContext())));
                break;
            case R.id.player_button_volume_up:
                MusicPlayerControl.sendControlCommand(new VolumeUpCommand(VolumeButtonsHelper.getVolumeAmount(this.getContext())));
                break;
            case R.id.player_button_repeat:
                MusicPlayerControl.sendControlCommand(new RepeatCommand(!playerStatus.getRepeat()));
                break;
            case R.id.player_button_consume:
                MusicPlayerControl.sendControlCommand(new ConsumeCommand(!playerStatus.getConsume()));
                if (showPlayerOptionToast()) {
                    Boast.makeText(getActivity(), playerStatus.getConsume() ? R.string.player_consume_off_toast : R.string.player_consume_on_toast).show();
                }
                break;
            case R.id.player_button_random:
                MusicPlayerControl.sendControlCommand(new RandomCommand(!playerStatus.getRandom()));
                break;
            case R.id.player_button_previous:
                MusicPlayerControl.sendControlCommand(new PreviousCommand());
                break;
            case R.id.player_button_stop:
                MusicPlayerControl.sendControlCommand(new StopCommand());
                break;
            case R.id.player_button_play:
                MusicPlayerControl.sendControlCommand(playerStatus.getState() == PlayerState.STOP ? new PlayCommand() : new PauseCommand(playerStatus.getState() == PlayerState.PAUSE));
                break;
            case R.id.player_button_next:
                MusicPlayerControl.sendControlCommand(new NextCommand());
                break;
        }
    }

    private void refreshPlayingInfo() {
        TextView artistTextView = findTextView(R.id.player_text_info_artist);
        TextView albumTextView = findTextView(R.id.player_text_info_album);
        TextView titleTextView = findTextView(R.id.player_text_info_title);

        PlaylistEntity playingEntity = getPlayingEntity();
        if (playingEntity != null) {
            CharSequence oldArtist = artistTextView.getText();
            CharSequence oldAlbum = albumTextView.getText();

            String artist = playingEntity.getArtist();
            artistTextView.setText(artist != null ? artist : "");

            String album = playingEntity.getAlbum();
            if (album != null) {
                albumTextView.setText(album);
            } else if (Utils.nullOrEmpty(artist)) {
                String name = playingEntity.getName();
                if (name != null) {
                    albumTextView.setText(name);
                } else {
                    UriEntity uri = playingEntity.getUriEntity();
                    if (uri != null) {
                        albumTextView.setText(uri.getUriFilname());
                    } else {
                        albumTextView.setText("");
                    }
                }
            } else {
                albumTextView.setText("");
            }

            Integer track = playingEntity.getTrack();
            String title = playingEntity.getTitle();
            if (track != null && title != null) {
                titleTextView.setText(String.format("%s - %s", track, title));
            } else if (title != null) {
                titleTextView.setText(title);
            } else {
                UriEntity uri = playingEntity.getUriEntity();
                if (uri != null) {
                    titleTextView.setText(uri.getUriFilname());
                } else {
                    titleTextView.setText("");
                }
            }

            if (!Utils.equals(oldArtist, artist) || !Utils.equals(oldAlbum, album)) {
                ImageView imageView = findCoverView();
                if (imageView != null) {
                    imageView.setImageBitmap(null);
                }

                getCoverHandle.cancelTask();
                getCoverHandle = ExternalInformationService.getCover(artist, album, null, new CoverReceiver() {
                    @Override
                    public void receiveCover(Bitmap bitmap) {
                        if (bitmap != null) {
                            ImageView imageView = findCoverView();
                            if (imageView != null) {
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    }
                });
            }
        } else {
            artistTextView.setText("");
            albumTextView.setText("");
            titleTextView.setText(R.string.player_playing_info_none);

            ImageView imageView = findCoverView();
            if (imageView != null) {
                imageView.setImageBitmap(null);
            }
        }

        hideEmptyTextView(artistTextView);
        hideEmptyTextView(albumTextView);
    }

    private ImageView findCoverView() {
        View view = getView();
        if (view != null) {
            return (ImageView)view.findViewById(R.id.player_cover_image);
        }
        return null;
    }

    private PlaylistEntity getPlayingEntity() {
        int songIndex = Math.max(0, playerStatus.getCurrentSong());
        if (playlistEntities != null && songIndex < playlistEntities.length) {
            return playlistEntities[songIndex];
        }
        return null;
    }

    @SuppressLint("DefaultLocale")
    private void refreshTime(long elapsed, long total) {
        TextView timeTotal = findTextView(R.id.player_text_seek_total);
        String totalText = String.format("%d:%02d", total / 60, total % 60);
        timeTotal.setText(totalText);

        SeekBar seekBar = findSeekBar();
        seekBar.setMax((int)total);
        if (!seeking) {
            updateElapsedTimeText(elapsed);
            if (elapsed >= 0 && elapsed <= total) {
                seekBar.setProgress((int)elapsed);
            } else {
                seekBar.setProgress(0);
            }
        }
    }

    private void hideEmptyTextView(TextView textView) {
        CharSequence text = textView.getText();
        if (text == null || text.length() == 0) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }
    }

    private TextView findTextView(int id) {
        return (TextView) getView().findViewById(id);
    }

    private void setupButtonClickListener(View view, int id) {
        View button = (View) view.findViewById(id);
        button.setOnClickListener(this);
    }

    private void setPlayButtonState(PlayerState state) {
        ImageButton button = (ImageButton)getView().findViewById(R.id.player_button_play);
        if (state == PlayerState.STOP) {
            toggleButton(button, false);
            button.setImageResource(R.drawable.ic_play);
        } else if (state == PlayerState.PLAY) {
            toggleButton(button, false);
            button.setImageResource(R.drawable.ic_pause);
        } else if (state == PlayerState.PAUSE) {
            toggleButton(button, true);
            button.setImageResource(R.drawable.ic_pause);
        }
    }

    private SeekBar findSeekBar() {
        SeekBar seekBar = (SeekBar) getView().findViewById(R.id.player_seek_bar_time);
        return seekBar;
    }

    private void setSeekBarEnablement(SeekBar seekBar) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean enabled = sharedPreferences.getBoolean(PREFERENCE_SEEK_BAR_ENABLED_KEY, true);

        seekBar.setEnabled(enabled);
    }

    private void toggleSeekBarEnablement() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean enabled = !sharedPreferences.getBoolean(PREFERENCE_SEEK_BAR_ENABLED_KEY, true);

        Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREFERENCE_SEEK_BAR_ENABLED_KEY, enabled);
        editor.commit();

        SeekBar seekBar = findSeekBar();
        seekBar.setEnabled(enabled);
    }

    private void showNext() {
        String toastText;
        int songIndex = Math.max(0, playerStatus.getNextSong());
        if (playerStatus.getState() != PlayerState.STOP && playlistEntities != null && songIndex < playlistEntities.length) {
            PlaylistEntity nextEntity = playlistEntities[songIndex];
            toastText = nextEntity.getArtist() + " - " + nextEntity.getTitle();
        } else {
            toastText = "?";
        }
        Boast toast = Boast.makeText(getActivity(), toastText);
//        toast.setGravity(Gravity.CENTER, 0, -40);
        toast.show();
    }

    private void goTo() {
        String[] items = getResources().getStringArray(R.array.player_context_goto);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.player_goto_header);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        PlaylistEntity playingEntity = getPlayingEntity();
                        if (playingEntity != null) {
                            String artist = playingEntity.getArtist();
                            if (!Utils.nullOrEmpty(artist)) {
                                Intent intent = new Intent(getActivity(), FilteredAlbumAndTitleActivity.class);
                                Bundle args = new Bundle();
                                args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, artist);
                                args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, LibraryEntity.createBuilder().setArtist(artist).build());
                                intent.putExtras(args);
                                startActivity(intent);
                            } else {
                                Boast.makeText(getActivity(), R.string.player_not_possible).show();
                            }
                        } else {
                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                        }
                    }
                    break;
                    case 1: {
                        PlaylistEntity playingEntity = getPlayingEntity();
                        if (playingEntity != null) {
                            String album = playingEntity.getAlbum();
                            if (!Utils.nullOrEmpty(album)) {
                                Intent intent = new Intent(getActivity(), FilteredTrackAndTitleActivity.class);
                                Bundle args = new Bundle();
                                args.putString(FilteredActivity.TITLE_ARGUMENT_KEY, album);
                                args.putSerializable(FilteredActivity.ENTITY_ARGUMENT_KEY, LibraryEntity.createBuilder().setAlbum(album).build());
                                intent.putExtras(args);
                                startActivity(intent);
                            } else {
                                Boast.makeText(getActivity(), R.string.player_not_possible).show();
                            }
                        } else {
                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                        }
                    }
                    break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void goToLastFm() {
        String[] items = getResources().getStringArray(R.array.player_lastfm_goto_items);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.player_lastfm_goto_header);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        PlaylistEntity playingEntity = getPlayingEntity();
                        if (playingEntity != null) {
                            String artist = playingEntity.getArtist();
                            if (!Utils.nullOrEmpty(artist)) {
                                lastFmHandle.cancelTask();
                                lastFmHandle = ExternalInformationService.getArtistInfoUrls(artist, new UrlReceiver() {
                                    @Override
                                    public void receiveUrls(String[] urls) {
                                        if (urls != null && urls.length > 0) {
                                            String url = urls[0];
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                        } else {
                                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                                        }
                                    }
                                });
                            } else {
                                Boast.makeText(getActivity(), R.string.player_not_possible).show();
                            }
                        } else {
                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                        }
                    }
                    break;
                    case 1: {
                        PlaylistEntity playingEntity = getPlayingEntity();
                        if (playingEntity != null) {
                            String artist = playingEntity.getArtist();
                            String album = playingEntity.getAlbum();
                            if (!Utils.nullOrEmpty(artist) && !Utils.nullOrEmpty(album)) {
                                lastFmHandle.cancelTask();
                                lastFmHandle = ExternalInformationService.getAlbumInfoUrls(artist, album, new UrlReceiver() {
                                    @Override
                                    public void receiveUrls(String[] urls) {
                                        if (urls != null && urls.length > 0) {
                                            String url = urls[0];
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                        } else {
                                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                                        }
                                    }
                                });
                            } else {
                                Boast.makeText(getActivity(), R.string.player_not_possible).show();
                            }
                        } else {
                            Boast.makeText(getActivity(), R.string.player_not_possible).show();
                        }
                    }
                    break;
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void setVolumeText(int volume) {
        TextView textView = (TextView)getView().findViewById(R.id.player_text_volume);
        if (volume != -1) {
            textView.setText(getString(R.string.player_volume_text_format, volume));
        } else {
            textView.setText(getString(R.string.player_volume_text_no_mixer));
        }
    }

    private void updateElapsedTimeText(long elapsed) {
        TextView timeTotal = findTextView(R.id.player_text_seek_total);
        CharSequence totalText = timeTotal.getText();

        int leadingZeroes = Math.max(1, totalText.length() - ":00".length());
        TextView timeElapsed = findTextView(R.id.player_text_seek_elapsed);
        timeElapsed.setText(String.format("%0" + leadingZeroes + "d:%02d", elapsed / 60, elapsed % 60));
    }

    private void toggleButton(int id, boolean toggled) {
        ImageButton button = (ImageButton)getView().findViewById(id);
        toggleButton(button, toggled);
    }

    private boolean showPlayerOptionToast() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Resources resources = getActivity().getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_control_options_show_toast_key), true);
    }

    private void toggleButton(ImageButton button, boolean toggled) {
        ToggleButtonHelper.toggleButton(getActivity(), button, toggled);
    }

    private final class TimeBarChangedListener implements OnSeekBarChangeListener {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            seeking = true;
            updateTimeHandler.running(false);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                updateElapsedTimeText(progress);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            seeking = false;
            updateTimeHandler.running(false);
            if (playerStatus.getState() == PlayerState.PLAY || playerStatus.getState() == PlayerState.PAUSE) {
                PlaylistEntity playingEntity = getPlayingEntity();
                if (playingEntity != null) {
                    MusicPlayerControl.sendControlCommand(new SeekCommand(playingEntity.getId(), seekBar.getProgress()));
                }
            }
        }
    }

    private static final class UpdateTimeHandler extends Handler {

        public static final int UPDATE_TIME_EVENT = 20011;
        public static final int INTERVAL = 475;

        //keep a weak reference such that the fragment can be garbage collected even though
        //there might be a pending message for this handler. If this handler holds a strong
        //reference to fragment, it can't be garbage collected as messages hold a reference
        //to this handler
        private WeakReference<PlayerControlFragment> fragmentRef;
        private boolean running = false;

        public UpdateTimeHandler(PlayerControlFragment fragment) {
            this.fragmentRef = new WeakReference<PlayerControlFragment>(fragment);
        }

        public void running(boolean run) {
            if (!running && run) {
                dispatchNext();
            }
            running = run;

            if (!running) {
                removeMessages(UPDATE_TIME_EVENT);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME_EVENT:
                    if (running) {
                        PlayerControlFragment fragment = fragmentRef.get();
                        if (fragment != null) {
                            long delta = System.currentTimeMillis() - fragment.playerStatus.getTimestamp();
                            fragment.refreshTime(fragment.playerStatus.getElapsedTime() + (delta / 1000), fragment.playerStatus.getTotalTime());
                            dispatchNext();
                        }
                    }
                    break;
            }
        }

        private void dispatchNext() {
            Message message = obtainMessage(UPDATE_TIME_EVENT);
            sendMessageDelayed(message, INTERVAL);
        }
    }
}
