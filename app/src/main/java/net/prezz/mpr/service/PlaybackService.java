package net.prezz.mpr.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.IBinder;

import androidx.preference.PreferenceManager;

import net.prezz.mpr.R;
import net.prezz.mpr.Utils;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.PlaylistEntity;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.StatusListener;
import net.prezz.mpr.model.TaskHandle;
import net.prezz.mpr.model.UriEntity;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.NextCommand;
import net.prezz.mpr.model.command.PlayPauseCommand;
import net.prezz.mpr.model.command.PreviousCommand;
import net.prezz.mpr.model.command.VolumeDownCommand;
import net.prezz.mpr.model.command.VolumeUpCommand;
import net.prezz.mpr.model.external.CoverReceiver;
import net.prezz.mpr.model.external.ExternalInformationService;
import net.prezz.mpr.mpd.MpdPlayer;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;
import net.prezz.mpr.ui.player.PlayerActivity;

import java.util.Arrays;

public class PlaybackService extends Service {

    private static final String CMD_VOL_DOWN = "net.prezz.mpr.service.PlaybackService.CMD_VOL_DOWN";
    private static final String CMD_VOL_UP = "net.prezz.mpr.service.PlaybackService.CMD_VOL_UP";
    private static final String CMD_PREV = "net.prezz.mpr.service.PlaybackService.CMD_PREV";
    private static final String CMD_PLAY_PAUSE = "net.prezz.mpr.service.PlaybackService.CMD_PLAY_PAUSE";
    private static final String CMD_NEXT = "net.prezz.mpr.service.PlaybackService.CMD_NEXT";

    private static final int NOTIFICATION_ID = 64545;

    private static final Object lock = new Object();
    private static boolean started = false;

    private boolean isForegroundService;
    private MpdPlayer player;
    private PlayerStatus playerStatus;
    private PlaylistEntity playlistEntity;
    private Bitmap cover;
    private PlayerInfoRefreshListener playerInfoRefreshListener;
    private TaskHandle updatePlaylistHandle;
    private TaskHandle updateCoverHandle;
    private ControlBroadcastReceiver broadcastReceiver;
    private int coverSize;


    public static void start() {
        synchronized (lock) {
            Context context = ApplicationActivator.getContext();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Resources resources = context.getResources();
            boolean enabled = sharedPreferences.getBoolean(resources.getString(R.string.settings_behavior_show_notification_key), false);

            if (enabled && !started) {
                started = true;
                context.startService(new Intent(context, PlaybackService.class));
            }
        }
    }

    public static void stop() {
        synchronized (lock) {
            Context context = ApplicationActivator.getContext();
            context.stopService(new Intent(context, PlaybackService.class));
            started = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        updatePlaylistHandle = TaskHandle.NULL_HANDLE;
        updateCoverHandle = TaskHandle.NULL_HANDLE;

        coverSize = (int) (getResources().getDisplayMetrics().density * 64);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        started = true; // if the service gets killed and restarted we need to set it to true

        isForegroundService = false;
        playlistEntity = null;

        MpdPlayerSettings settings = MpdPlayerSettings.create(this);

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        broadcastReceiver = new ControlBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CMD_VOL_DOWN);
        filter.addAction(CMD_VOL_UP);
        filter.addAction(CMD_PREV);
        filter.addAction(CMD_PLAY_PAUSE);
        filter.addAction(CMD_NEXT);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, filter, RECEIVER_EXPORTED);

        if (player != null) {
            updateCoverHandle.cancelTask();
            updatePlaylistHandle.cancelTask();
            player.dispose();
        }

        updateNotification(isForegroundService);

        player = new MpdPlayer(settings);
        playerStatus = new PlayerStatus(false);

        playerInfoRefreshListener = new PlayerInfoRefreshListener();
        player.setStatusListener(playerInfoRefreshListener);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        updateCoverHandle.cancelTask();
        updatePlaylistHandle.cancelTask();
        if (player != null) {
            player.dispose();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        started = false;

        super.onDestroy();
    }

    private void updateNotification(boolean sticky) {
        String artistText = null;
        String titleText = null;
        if (playlistEntity != null) {
            String artist = playlistEntity.getArtist();
            if (artist != null) {
                artistText = artist;
            } else {
                String name = playlistEntity.getName();
                if (name != null) {
                    artistText = name;
                } else {
                    artistText = "";
                }
            }

            Integer track = playlistEntity.getTrack();
            String title = playlistEntity.getTitle();
            if (track != null && title != null) {
                titleText = String.format("%s - %s", track, title);
            } else if (title != null) {
                titleText = title;
            } else {
                UriEntity uri = playlistEntity.getUriEntity();
                if (uri != null) {
                    titleText = uri.getUriFilname();
                } else {
                    titleText = "";
                }
            }
        } else {
            artistText = "";
            titleText = getString(R.string.player_playing_info_none);
        }

        String volumeText = "";
        int volume = (playerStatus != null) ? playerStatus.getVolume() : -1;
        if (volume != -1) {
            volumeText = getString(R.string.general_volume_text_format, volume);
        } else {
            volumeText = getString(R.string.general_volume_text_no_mixer);
        }

        PlayerState playerState = (playerStatus != null) ? playerStatus.getState() : PlayerState.STOP;

        updateMediaNotification(artistText, titleText, volumeText, playerState, sticky);
    }

    private void updateMediaNotification(String artist, String title, String volume, PlayerState playerState, boolean sticky) {
        Intent volDownIntent = new Intent(CMD_VOL_DOWN);
        Intent volUpIntent = new Intent(CMD_VOL_UP);
        Intent prevIntent = new Intent(CMD_PREV);
        Intent playPauseIntent = new Intent(CMD_PLAY_PAUSE);
        Intent nextIntent = new Intent(CMD_NEXT);
        Intent launchIntent = new Intent(this, PlayerActivity.class);

        int ic_play = (playerState == PlayerState.PLAY) ? R.drawable.ic_pause_w : (playerState == PlayerState.PAUSE) ? R.drawable.ic_paused_w : R.drawable.ic_play_w;

        String channelId = getString(R.string.notification_media_player_channel_id);

        Notification notification = new Notification.Builder(this, channelId)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_notification)
                .setShowWhen(false)

                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_volume_down_w), "", PendingIntent.getBroadcast(this, 0, volDownIntent, PendingIntent.FLAG_IMMUTABLE)).build())  // #0
                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_volume_up_w), "", PendingIntent.getBroadcast(this, 0, volUpIntent, PendingIntent.FLAG_IMMUTABLE)).build())      // #1
                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_previous_w), "", PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE)).build())        // #2
                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, ic_play), "", PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE)).build())                    // #3
                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_next_w), "", PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE)).build())            // #4

                .setStyle(new Notification.MediaStyle().setShowActionsInCompactView(3)) // #3 play toggle button
                .setContentTitle(title)
                .setContentText(artist)
                .setSubText(volume)
                .setLargeIcon(cover)
                .setContentIntent(PendingIntent.getActivity(this, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE))
                .setAutoCancel(false)
                .setOngoing(sticky)
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
        if (channel == null) {
            channel = new NotificationChannel(channelId, getString(R.string.notification_media_player_channel_name), NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        if (sticky) {
            startForeground(NOTIFICATION_ID, notification);
            isForegroundService = true;
        } else {
            if (isForegroundService) {
                stopForeground(STOP_FOREGROUND_DETACH);
            }

            notificationManager.notify(NOTIFICATION_ID, notification);
            isForegroundService = false;
        }
    }

    private class PlayerInfoRefreshListener extends ResponseReceiver<PlaylistEntity> implements StatusListener {

        @Override
        public void statusUpdated(PlayerStatus status) {
            if (!status.isConnected() || status.getState() == PlayerState.STOP) {
                PlaybackService.stop();
            } else {

                boolean updateState = (playerStatus.getState() != status.getState() || playerStatus.getVolume() != status.getVolume());
                boolean updatePlaying = (playlistEntity == null || playerStatus.getPlaylistVersion() != status.getPlaylistVersion() || playerStatus.getCurrentSong() != status.getCurrentSong());
                playerStatus = status;

                if (updateState) {
                    updateNotification(status.getState() == PlayerState.PLAY);
                }

                if (updatePlaying) {
                    int position = Math.max(0, status.getCurrentSong());
                    updatePlaylistHandle.cancelTask();
                    updatePlaylistHandle = player.getPlaylistEntity(position, this);
                }
            }
        }

        @Override
        public void receiveResponse(PlaylistEntity response) {
            boolean updateCover = false;
            if (response != null && playlistEntity == null) {
                updateCover = true;
            } else if (response == null && playlistEntity != null) {
                updateCover = true;
            } else if (response != null && playlistEntity != null) {
                updateCover = !Utils.equals(response.getArtist(), playlistEntity.getArtist()) || !Utils.equals(response.getAlbum(), playlistEntity.getAlbum());
            }

            playlistEntity = response;

            if (updateCover) {
                cover = null;
            }

            updateNotification(isForegroundService);

            if (updateCover && response != null) {
                updateCoverHandle.cancelTask();
                updateCoverHandle = ExternalInformationService.getCover(response.getArtist(), response.getAlbum(), coverSize, new CoverReceiver() {
                    @Override
                    public void receiveCover(Bitmap bitmap) {
                        cover = bitmap;
                        updateNotification(isForegroundService);
                    }
                });
            }
        }
    }

    private class ControlBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CMD_VOL_DOWN.equals(action)) {
                sendControlCommand(new VolumeDownCommand(VolumeButtonsHelper.getVolumeAmount(context)));
            } else if (CMD_VOL_UP.equals(action)) {
                sendControlCommand(new VolumeUpCommand(VolumeButtonsHelper.getVolumeAmount(context)));
            } else if (CMD_PREV.equals(action)) {
                sendControlCommand(new PreviousCommand());
            } else if (CMD_PLAY_PAUSE.equals(action)) {
                sendControlCommand(new PlayPauseCommand());
            } else if (CMD_NEXT.equals(action)) {
                sendControlCommand(new NextCommand());
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                player.setStatusListener(playerInfoRefreshListener);
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                player.setStatusListener(null);
            }
        }

        private void sendControlCommand(Command command) {
            player.sendControlCommands(Arrays.asList(command), new ResponseReceiver<ResponseResult>() {
                @Override
                public void receiveResponse(ResponseResult response) {
                }
            });
        }
    }
}
