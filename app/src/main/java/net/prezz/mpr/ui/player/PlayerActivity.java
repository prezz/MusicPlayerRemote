package net.prezz.mpr.ui.player;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.AudioOutput;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.StatusListener;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.model.command.PlayCommand;
import net.prezz.mpr.model.command.ToggleOutputCommand;
import net.prezz.mpr.model.servers.ServerConfiguration;
import net.prezz.mpr.model.servers.ServerConfigurationService;
import net.prezz.mpr.mpd.MpdPlayer;
import net.prezz.mpr.service.PlaybackService;
import net.prezz.mpr.service.StreamingService;
import net.prezz.mpr.ui.DatabaseActivity;
import net.prezz.mpr.ui.helpers.ThemeHelper;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.library.LibraryActivity;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;
import net.prezz.mpr.ui.partitions.PartitionsActivity;
import net.prezz.mpr.ui.playlists.StoredPlaylistsActivity;
import net.prezz.mpr.ui.search.SearchActivity;
import net.prezz.mpr.ui.settings.SettingsActivity;
import net.prezz.mpr.ui.view.DataFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerActivity extends AppCompatActivity implements ActivityResultCallback<ActivityResult> {

    private static final String PREFERENCE_SHOW_SWIPE_HINT_KEY = "player_show_swipe_hint";

    private static final String PLAYER_STATUS_INSTANCE_STATE = "playerStatus";
    private static final String PLAYLIST_ENTITIES_INSTANCE_STATE = "playlistEntities";

    private MpdPlayerSettings currentMpdSettings;

    private final MusicPlayerRefreshListener musicPlayerRefreshListener = new MusicPlayerRefreshListener();
    private ViewPager2.OnPageChangeCallback pageChangeCallback = new PageChangeCallback();
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private PlayerFragment[] attachedFragments = new PlayerFragment[2];
    private int fragmentPosition;
    private PlayerStatus playerStatus;
    private PlaylistEntity[] playlistEntities;
    private boolean darkTheme = false;

    private Menu optionsMenu;

    private TaskHandle updatePlaylistHandle = TaskHandle.NULL_HANDLE;
    private TaskHandle selectOutputsHandle = TaskHandle.NULL_HANDLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        darkTheme = ThemeHelper.applyTheme(this);
        setContentView(R.layout.activity_player);


        final PlayerPagerAdapter pageAdapter = new PlayerPagerAdapter(this);
        ViewPager2 viewPager = (ViewPager2) findViewById(R.id.player_view_pager_swipe);
        viewPager.setAdapter(pageAdapter);

        fragmentPosition = getDefaultFragment();
        if (fragmentPosition > 0) {
            viewPager.setCurrentItem(fragmentPosition);
        }

        viewPager.registerOnPageChangeCallback(pageChangeCallback);
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this);

        DataFragment dataFragment = DataFragment.getRestoreFragment(this, getClass());
        if (dataFragment != null) {
            playerStatus = (PlayerStatus) dataFragment.getData(PLAYER_STATUS_INSTANCE_STATE, null);
            Object[] objectEntities = (Object[]) dataFragment.getData(PLAYLIST_ENTITIES_INSTANCE_STATE, null);
            if (objectEntities != null) {
                playlistEntities = Arrays.copyOf(objectEntities, objectEntities.length, PlaylistEntity[].class);
            }
        }

        setActivityTitle();

        if (connectMusicPlayer()) {
            showSwipeHint();
        }

        if (playerStatus == null) {
            playerStatus = new PlayerStatus(false);
        }

        if (playlistEntities == null) {
            playlistEntities = new PlaylistEntity[0];
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MusicPlayerControl.setStatusListener(musicPlayerRefreshListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        MusicPlayerControl.setStatusListener(null);
        updatePlaylistHandle.cancelTask();
        selectOutputsHandle.cancelTask();
    }

    @Override
    public void onDestroy() {
        ViewPager2 viewPager = (ViewPager2) findViewById(R.id.player_view_pager_swipe);
        viewPager.unregisterOnPageChangeCallback(pageChangeCallback);

        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        DataFragment dataFragment = DataFragment.getSaveFragment(this, getClass());
        if (dataFragment != null) {
            dataFragment.setData(PLAYER_STATUS_INSTANCE_STATE, playerStatus);
            dataFragment.setData(PLAYLIST_ENTITIES_INSTANCE_STATE, playlistEntities);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);

        this.optionsMenu = menu;
        updateOptionsMenu(currentMpdSettings);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.player_action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                activityResultLauncher.launch(intent);
                return true;
            }
            case R.id.player_action_server: {
                selectServer();
                return true;
            }
            case R.id.player_action_database: {
                Intent intent = new Intent(this, DatabaseActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.player_action_partitions: {
                Intent intent = new Intent(this, PartitionsActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.player_action_outputs: {
                selectOutputs();
                return true;
            }
            case R.id.player_action_toggle_streaming: {
                if (StreamingService.isStarted()) {
                    stopStreaming();
                } else {
                    String streamingUrl = currentMpdSettings.getMpdStreamingUrl();
                    if (!Utils.nullOrEmpty(streamingUrl)) {
                        startStreaming(streamingUrl);
                    }
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(ActivityResult result) {
        if (ThemeHelper.useDarkTheme(this) != darkTheme) {
            finish();
            startActivity(new Intent(this, PlayerActivity.class));
        } else {
            if (reconnectMusicPlayerOnSettingsChanged(false)) {
                showSwipeHint();
            } else {
                attachedFragments[PlayerPlaylistFragment.FRAGMENT_POSITION].playlistUpdated(playlistEntities);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (VolumeButtonsHelper.handleKeyDown(this, keyCode, event)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void attachFragment(PlayerFragment fragment, int pos) {
        attachedFragments[pos] = fragment;

        fragment.statusUpdated(playerStatus);
        fragment.playlistUpdated(playlistEntities);
    }

    public void detachFragment(int pos) {
        attachedFragments[pos] = null;
    }

    public void onStoredPlaylistsClick(View v) {
        Intent intent = new Intent(this, StoredPlaylistsActivity.class);
        startActivity(intent);
    }

    public void onLibraryClick(View v) {
        Intent intent = new Intent(this, LibraryActivity.class);
        startActivity(intent);
    }

    public void onChoiceMenuClick(View view) {
        attachedFragments[fragmentPosition].onChoiceMenuClick(view);
    }

    public void onSearchClick(View v) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void onSelectOutput() {
        selectOutputs();
    }

    private void selectServer() {
        final ServerConfiguration[] serverConfigurations = ServerConfigurationService.getServerConfigurations();
        final ServerConfiguration selectedConfiguration = ServerConfigurationService.getSelectedServerConfiguration();

        final int[] selectedItem = new int[] { -1 };
        String[] items = new String[serverConfigurations.length];
        for (int i = 0; i < serverConfigurations.length; i++) {
            items[i] = serverConfigurations[i].toString();
            if (Utils.equals(selectedConfiguration, serverConfigurations[i])) {
                selectedItem[0] = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
        builder.setTitle(R.string.player_action_server);
        builder.setSingleChoiceItems(items, selectedItem[0], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedItem[0] = which;
            }
        });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedItem[0] != -1) {
                    ServerConfiguration selected = serverConfigurations[selectedItem[0]];
                    if (!Utils.equals(selectedConfiguration, selected)) {
                        ServerConfigurationService.setSelectedServerConfiguration(selected);
                        reconnectMusicPlayerOnSettingsChanged(true);
                    }
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void selectOutputs() {
        selectOutputsHandle.cancelTask();
        selectOutputsHandle = MusicPlayerControl.getOutputs(false, new ResponseReceiver<AudioOutput[]>() {
            @Override
            public void receiveResponse(final AudioOutput[] response) {
                String[] items = new String[response.length];
                final boolean[] preChecked = new boolean[response.length];
                final boolean[] postChecked = new boolean[response.length];
                for (int i = 0; i < response.length; i++) {
                    String pluginSuffix = response[i].getPlugin().length() > 0 ? " (" + response[i].getPlugin() + ")" : "";
                    items[i] = response[i].getOutputName() + pluginSuffix;
                    preChecked[i] = response[i].isEnabled();
                    postChecked[i] = response[i].isEnabled();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
                builder.setTitle(R.string.player_action_outputs);
                builder.setMultiChoiceItems(items, postChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        postChecked[which] = isChecked;
                    }
                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<Command> commands = new ArrayList<Command>();

                        for (int i = 0; i < response.length; i++) { // first add all commands that enables
                            if (preChecked[i] != postChecked[i] && postChecked[i]) {
                                commands.add(new ToggleOutputCommand(response[i].getOutputId(), postChecked[i]));
                            }
                        }
                        for (int i = 0; i < response.length; i++) { // then the commands that disables
                            if (preChecked[i] != postChecked[i] && !postChecked[i]) {
                                commands.add(new ToggleOutputCommand(response[i].getOutputId(), postChecked[i]));
                            }
                        }

                        if (!commands.isEmpty()) {
                            MusicPlayerControl.sendControlCommands(commands);
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private boolean connectMusicPlayer() {
        currentMpdSettings = MpdPlayerSettings.create(getApplicationContext());
        if (currentMpdSettings.getMpdHost().isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            builder.setCancelable(false);
            builder.setTitle(R.string.player_no_server_configured_header);
            builder.setMessage(R.string.player_no_server_configured_message);
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setPositiveButton(R.string.player_action_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(PlayerActivity.this, SettingsActivity.class);
                    activityResultLauncher.launch(intent);
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
            return false;
        } else {
            MusicPlayerControl.setMusicPlayer(new MpdPlayer(currentMpdSettings));
            return true;
        }
    }

    private boolean reconnectMusicPlayerOnSettingsChanged(boolean connectStatusListener) {
        MpdPlayerSettings mpdSettings = MpdPlayerSettings.create(getApplicationContext());
        if (!mpdSettings.equals(currentMpdSettings)) {
            PlaybackService.stop();
            updateOptionsMenu(mpdSettings);
            currentMpdSettings = mpdSettings;
            if (!currentMpdSettings.getMpdHost().isEmpty()) {
                MusicPlayerControl.setMusicPlayer(new MpdPlayer(currentMpdSettings));
                if (connectStatusListener) {
                    MusicPlayerControl.setStatusListener(musicPlayerRefreshListener);
                }
                return true;
            } else {
                MusicPlayerControl.setMusicPlayer(null);
                playlistEntities = new PlaylistEntity[0];
                playerStatus = new PlayerStatus(false);
                return false;
            }
        }

        return false;
    }

    private void startStreaming(String url) {
        if (playerStatus.getState() == PlayerState.STOP) {
            MusicPlayerControl.sendControlCommand(new PlayCommand());
        }

        StreamingService.start(url);
    }

    private void stopStreaming() {
        if (playerStatus.getState() == PlayerState.PLAY) {
            MusicPlayerControl.sendControlCommand(new PauseCommand(false));
        }

        StreamingService.stop();
    }

    private int getDefaultFragment() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = this.getResources();
        return Integer.parseInt(sharedPreferences.getString(resources.getString(R.string.settings_default_player_fragment_key), "0"));
    }

    private void setActivityTitle() {
        ViewPager2 viewPager = (ViewPager2) findViewById(R.id.player_view_pager_swipe);
        if (viewPager != null) {
            PlayerPagerAdapter pageAdapter = (PlayerPagerAdapter) viewPager.getAdapter();
            String title = pageAdapter.getTitle(fragmentPosition);
            if (fragmentPosition == 0) {
                title += getPlaylistTime();
            }
            setTitle(title);
        }
    }

    private void updateOptionsMenu(MpdPlayerSettings mpdSettings) {
        if (optionsMenu != null) {
            MenuItem streamingItem = optionsMenu.findItem(R.id.player_action_toggle_streaming);
            if (streamingItem != null) {
                streamingItem.setVisible(!Utils.nullOrEmpty(mpdSettings.getMpdStreamingUrl()));
                onPrepareOptionsMenu(optionsMenu);
            }
        }
    }

    private String getPlaylistTime() {
        int totalTime = 0;

        if (playlistEntities != null) {
            for (int i = 0; i < playlistEntities.length; i++) {
                Integer time = playlistEntities[i].getTime();
                if (time != null) {
                    totalTime += time;
                }
            }
        }

        int hours = totalTime / 3600;
        int remaining = totalTime % 3600;
        int minutes = remaining / 60;
        int seconds = remaining % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(String.format(" (%d:%02d:%02d)", hours, minutes, seconds));
        } else {
            sb.append(String.format(" (%d:%02d)", minutes, seconds));
        }

        return sb.toString();
    }

    private void showSwipeHint() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show = sharedPreferences.getBoolean(PREFERENCE_SHOW_SWIPE_HINT_KEY, true);

        if (show) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
            builder.setCancelable(false);
            builder.setTitle(R.string.player_swipe_hint_header);
            builder.setMessage(R.string.player_swipe_hint_message);
            builder.setPositiveButton(R.string.player_swipe_hint_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(PREFERENCE_SHOW_SWIPE_HINT_KEY, false);
                    editor.commit();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private final class MusicPlayerRefreshListener extends ResponseReceiver<PlaylistEntity[]> implements StatusListener {

        private PlayerStatus pendingPlayerStatus;

        @Override
        public void statusUpdated(PlayerStatus status) {
            if (playerStatus.getPlaylistVersion() != status.getPlaylistVersion()) {
                pendingPlayerStatus = status;
                updatePlaylistHandle.cancelTask();
                updatePlaylistHandle = MusicPlayerControl.getPlaylist(this);
            } else {
                playerStatus = status;
                for (PlayerFragment fragment : attachedFragments) {
                    if (fragment != null) {
                        fragment.statusUpdated(playerStatus);
                    }
                }
            }
        }

        @Override
        public void receiveResponse(PlaylistEntity[] response) {
            playerStatus = pendingPlayerStatus;
            for (PlayerFragment fragment : attachedFragments) {
                if (fragment != null) {
                    fragment.statusUpdated(playerStatus);
                }
            }

            playlistEntities = response;
            for (PlayerFragment fragment : attachedFragments) {
                if (fragment != null) {
                    fragment.playlistUpdated(playlistEntities);
                }
            }

            setActivityTitle();
        }
    }

    private final class PageChangeCallback extends ViewPager2.OnPageChangeCallback {
        @Override
        public void onPageSelected(int position) {
            fragmentPosition = position;
            attachedFragments[fragmentPosition].forceRefresh();
            setActivityTitle();
        }
    }
}
