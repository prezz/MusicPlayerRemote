package net.prezz.mpr.ui.helpers;

import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.StatusListener;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.command.NextCommand;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.PreviousCommand;
import net.prezz.mpr.model.command.VolumeDownCommand;
import net.prezz.mpr.model.command.VolumeUpCommand;
import net.prezz.mpr.R;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MiniControlHelper implements StatusListener {

    private Activity activity;
    private TaskHandle taskHandler = TaskHandle.NULL_HANDLE;
    private PlayerStatus playerStatus = new PlayerStatus(false);


    public MiniControlHelper(Activity owner) {
        this.activity = owner;
        attachButtonListeners();
    }

    public void toggleVisibility() {
        View view = activity.findViewById(R.id.control_layout_mini_control);
        if (view != null) {
            switch (view.getVisibility()) {
                case View.GONE:
                    view.setVisibility(View.VISIBLE);
                    MusicPlayerControl.setStatusListener(this);
                    break;
                case View.VISIBLE:
                    doHideVisability(view);
                    break;
            }
        }
    }

    public void hideVisibility() {
        View view = activity.findViewById(R.id.control_layout_mini_control);
        doHideVisability(view);
    }

    @Override
    public void statusUpdated(PlayerStatus status) {
        if (playerStatus.getVolume() != status.getVolume()) {
            setVolumeText(status.getVolume());
        }

        if (playerStatus.getState() != status.getState()) {
            setPlayButtonState(status.getState());
        }

        boolean refreshPlaying = playerStatus.getPlaylistVersion() != status.getPlaylistVersion() || (playerStatus.getCurrentSong() != status.getCurrentSong());
        playerStatus = status;
        if (refreshPlaying) {
            refreshPlayingInfo();
        }
    }

    private void doHideVisability(View view) {
        if (view != null) {
            taskHandler.cancelTask();
            MusicPlayerControl.setStatusListener(null);
            view.setVisibility(View.GONE);
        }
    }

    private void setVolumeText(int volume) {
        TextView textView = (TextView)activity.findViewById(R.id.control_text_volume);
        if (volume != -1) {
            textView.setText(activity.getString(R.string.control_volume_text_format, volume));
        } else {
            textView.setText(activity.getString(R.string.control_volume_text_no_mixer));
        }
    }

    private void refreshPlayingInfo() {
        int songIndex = Math.max(0, playerStatus.getCurrentSong());
        taskHandler.cancelTask();
        taskHandler = MusicPlayerControl.getPlaylistEntity(songIndex, new ResponseReceiver<PlaylistEntity>() {
            @Override
            public void receiveResponse(PlaylistEntity response) {
                TextView artistTextView = (TextView)activity.findViewById(R.id.control_text_playing_artist);
                TextView titleTextView = (TextView)activity.findViewById(R.id.control_text_playing_title);
                if (response != null) {
                    String artist = response.getArtist();
                    if (artist != null) {
                        artistTextView.setText(artist);
                    } else {
                        String name = response.getName();
                        if (name != null) {
                            artistTextView.setText(name);
                        } else {
                            artistTextView.setText("");
                        }
                    }

                    Integer track = response.getTrack();
                    String title = response.getTitle();
                    if (track != null && title != null) {
                        titleTextView.setText(String.format("%s - %s", track, title));
                    } else if (title != null) {
                        titleTextView.setText(title);
                    } else {
                        UriEntity uri = response.getUriEntity();
                        if (uri != null) {
                            titleTextView.setText(uri.getUriFilname());
                        } else {
                            titleTextView.setText("");
                        }
                    }
                } else {
                    artistTextView.setText("");
                    titleTextView.setText(R.string.control_playing_info_none);
                }
            }
        });
    }

    private void setPlayButtonState(PlayerState state) {
        ImageButton button = (ImageButton)activity.findViewById(R.id.control_button_play);
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

    private void toggleButton(ImageButton button, boolean toggled) {
        ToggleButtonHelper.toggleButton(activity, button, toggled);
    }

    private void attachButtonListeners() {
        ImageButton buttonVolumeDown = (ImageButton) activity.findViewById(R.id.control_button_volume_down);
        buttonVolumeDown.setOnClickListener(new VolumeDownClickListener());

        ImageButton buttonVolumeUp = (ImageButton) activity.findViewById(R.id.control_button_volume_up);
        buttonVolumeUp.setOnClickListener(new VolumeUpClickListener());

        ImageButton buttonPrevious = (ImageButton) activity.findViewById(R.id.control_button_previous);
        buttonPrevious.setOnClickListener(new PreviousClickListener());

        ImageButton buttonPlay = (ImageButton) activity.findViewById(R.id.control_button_play);
        buttonPlay.setOnClickListener(new PlayClickListener());

        ImageButton buttonNext = (ImageButton) activity.findViewById(R.id.control_button_next);
        buttonNext.setOnClickListener(new NextClickListener());
    }

    private final class VolumeDownClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            MusicPlayerControl.sendControlCommand(new VolumeDownCommand(VolumeButtonsHelper.getVolumeAmount(activity)));
        }
    }

    private final class VolumeUpClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            MusicPlayerControl.sendControlCommand(new VolumeUpCommand(VolumeButtonsHelper.getVolumeAmount(activity)));
        }
    }

    private final class PreviousClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            MusicPlayerControl.sendControlCommand(new PreviousCommand());
        }
    }

    private final class PlayClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            MusicPlayerControl.sendControlCommand(playerStatus.getState() == PlayerState.STOP ? new PlayCommand() : new PauseCommand(playerStatus.getState() == PlayerState.PAUSE));
        }
    }

    private final class NextClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            MusicPlayerControl.sendControlCommand(new NextCommand());
        }
    }
}
