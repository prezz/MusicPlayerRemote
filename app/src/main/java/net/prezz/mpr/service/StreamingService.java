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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import android.util.Log;

import net.prezz.mpr.R;
import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.PlayerStatus;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.StatusListener;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.NextCommand;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.model.command.PreviousCommand;
import net.prezz.mpr.mpd.MpdPlayer;
import net.prezz.mpr.ui.ApplicationActivator;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;
import net.prezz.mpr.ui.player.PlayerActivity;

import java.util.Arrays;

/**
 * https://developer.android.com/guide/topics/media/mediaplayer.html
 */
public class StreamingService extends Service {

    private static final String CMD_STOP = "net.prezz.mpr.service.StreamingService.CMD_STOP";
    private static final String CMD_PAUSE = "net.prezz.mpr.service.StreamingService.CMD_PAUSE";

    private static final int NOTIFICATION_ID = 864532;
    private static final String URL_ARGUMENT_KEY = "url";

    private static final Object lock = new Object();
    private static boolean started = false;

    private MpdPlayer mpdPlayer;
    private PlayerState mpdState;
    private MediaSession mediaSession;
    private MediaPlayer mediaPlayer;
    private WifiManager.WifiLock wifiLock;
    private PowerManager.WakeLock wakeLock;
    private String url;
    private MediaPlayerListener mediaPlayerListener;
    private StreamBroadcastReceiver broadcastReceiver;
    private boolean preparing = true;


    public static boolean isStarted() {
        return started;
    }

    public static void start(String url) {
        synchronized (lock) {
            Context context = ApplicationActivator.getContext();

            if (!started) {
                started = true;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, StreamingService.class).putExtra(URL_ARGUMENT_KEY, url));
                } else {
                    context.startService(new Intent(context, StreamingService.class).putExtra(URL_ARGUMENT_KEY, url));
                }
            } else {
                Boast.makeText(context, "Stream already running,");
            }
        }
    }

    public static void stop() {
        synchronized (lock) {
            Context context = ApplicationActivator.getContext();
            context.stopService(new Intent(context, StreamingService.class));
            started = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        started = true; // if the service gets killed and restarted we need to set it to true

        url = intent.getStringExtra(URL_ARGUMENT_KEY);

        Context context = getApplicationContext();

        if (wifiLock != null) {
            wifiLock.release();
        }

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "mpd_stream:wifi_lock");
        wifiLock.acquire();

        if (wakeLock != null) {
            wakeLock.release();
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "mpd_stream:wake_lock");
        wakeLock.acquire();

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        broadcastReceiver = new StreamBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CMD_STOP);
        filter.addAction(CMD_PAUSE);
        registerReceiver(broadcastReceiver, filter);

        startMediaSession();
        startMediaPlayer(context);

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        stopMediaSession();

        if (wakeLock != null) {
            wakeLock.release();
        }

        if (wifiLock != null) {
            wifiLock.release();
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);

        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }

        started = false;

        super.onDestroy();
    }

    private void updateNotification(String text) {
        updateMediaMetadata(text);
        updateMediaNotification(text);
    }

    private void updateMediaMetadata(String text) {

        MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, getString(R.string.notification_streaming_service_title))
                .putString(MediaMetadata.METADATA_KEY_ARTIST, text)
                .build();

        mediaSession.setMetadata(mediaMetadata);
    }

    private void updateMediaNotification(String text) {

        Intent stopIntent = new Intent(CMD_STOP);
        Intent pauseIntent = new Intent(CMD_PAUSE);
        Intent launchIntent = new Intent(this, PlayerActivity.class);

        int ic_play = (mediaPlayer.isPlaying()) ? R.drawable.ic_pause_w : R.drawable.ic_paused_w;

        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_stream)
                .setShowWhen(false)

                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, ic_play), "", PendingIntent.getBroadcast(this, 0, pauseIntent, 0)).build())  // #0
                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, R.drawable.ic_stop_w), "", PendingIntent.getBroadcast(this, 0, stopIntent, 0)).build())  // #1

                .setStyle(new Notification.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(1))
                .setContentTitle(getString(R.string.notification_streaming_service_title))
                .setContentText(text)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentIntent(PendingIntent.getActivity(this, 0, launchIntent, 0))
                .setAutoCancel(false)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getString(R.string.notification_streaming_channel_id);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel == null) {
                channel = new NotificationChannel(channelId, getString(R.string.notification_streaming_channel_name), NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
            }

            notificationBuilder.setChannelId(channelId);
        }

        Notification notification = notificationBuilder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    private void startMediaSession() {
        stopMediaSession();

        mediaSession = new MediaSession(this, "MPD Remote");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        }
        mediaSession.setCallback(new MediaSessionCallback());
        mediaSession.setActive(true);

        mpdState = null;

        MpdPlayerSettings settings = MpdPlayerSettings.create(this);
        mpdPlayer = new MpdPlayer(settings);
        mpdPlayer.setStatusListener(new PlayerInfoRefreshListener());
    }

    private void stopMediaSession() {
        if (mpdPlayer != null) {
            mpdPlayer.dispose();
        }

        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
        }
    }

    private void setMediaSessionState(PlayerState playerState) {
        final long COMMON_ACTION = PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS;

        PlaybackState.Builder playbackStateBuilder = new PlaybackState.Builder();

        if (playerState == PlayerState.PLAY) {
            playbackStateBuilder.setActions(COMMON_ACTION | PlaybackState.ACTION_PAUSE);
            playbackStateBuilder.setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
        } else if (playerState == PlayerState.PAUSE) {
            playbackStateBuilder.setActions(COMMON_ACTION | PlaybackState.ACTION_PLAY);
            playbackStateBuilder.setState(PlaybackState.STATE_PAUSED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
        } else {
            playbackStateBuilder.setState(PlaybackState.STATE_NONE, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 0);
        }

        mediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    private void startMediaPlayer(Context context) {
        preparing = true;

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

        mediaPlayerListener = new MediaPlayerListener();
        mediaPlayer.setOnPreparedListener(mediaPlayerListener);
        mediaPlayer.setOnBufferingUpdateListener(mediaPlayerListener);
        mediaPlayer.setOnCompletionListener(mediaPlayerListener);
        mediaPlayer.setOnInfoListener(mediaPlayerListener);
        mediaPlayer.setOnErrorListener(mediaPlayerListener);

        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            mediaPlayer.prepareAsync();
        } catch (Exception ex) {
            StreamingService.stop();
        }

        updateNotification(getString(R.string.notification_streaming_service_buffering));
    }

    private class MediaPlayerListener implements MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener,
            MediaPlayer.OnCompletionListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            Log.i(StreamingService.class.getName(), "Stream prepared");

            mediaPlayer.start();
            updateNotification(url);
            preparing = false;
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            Log.i(StreamingService.class.getName(), "Stream buffering " + percent + "%");
        }

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            Log.i(StreamingService.class.getName(), "Stream completed");

            if (mpdState == PlayerState.PLAY) {
                startMediaPlayer(getApplicationContext());
            } else {
                updateNotification(getString(R.string.notification_streaming_service_idle));
            }
        }

        @Override
        public boolean onInfo(MediaPlayer mediaPlayer, int what, int extra) {
            Log.i(StreamingService.class.getName(), "Stream info code " + what + ", " + extra);

            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                //Likely a dropped connection. Restart the media player.
                startMediaPlayer(getApplicationContext());
                return true;
            }

            return false;
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
            Log.i(StreamingService.class.getName(), "Stream error code " + what + ", " + extra);

            if (mpdState == PlayerState.PLAY) {
                startMediaPlayer(getApplicationContext());
            } else {
                updateNotification(getString(R.string.notification_streaming_service_idle));
            }

            return true;
        }
    }

    private class StreamBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (CMD_STOP.equals(action)) {
                StreamingService.stop();
            } else if (CMD_PAUSE.equals(action)) {
                if (!preparing) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                    updateNotification(url);
                }
            }
        }
    }

    private class PlayerInfoRefreshListener implements StatusListener {

        @Override
        public void statusUpdated(PlayerStatus status) {
            if (mpdState != status.getState()) {
                if (mpdState != null && status.getState() == PlayerState.PLAY) {
                    startMediaPlayer(getApplicationContext());
                }

                mpdState = status.getState();
                setMediaSessionState(mpdState);
            }
        }
    }

    private class MediaSessionCallback extends MediaSession.Callback {

        @Override
        public void onPlay() {
            sendControlCommand(new PauseCommand(true));
        }

        @Override
        public void onPause() {
            sendControlCommand(new PauseCommand(false));
        }

        @Override
        public void onSkipToNext() {
            sendControlCommand(new NextCommand());
        }

        @Override
        public void onSkipToPrevious() {
            sendControlCommand(new PreviousCommand());
        }

        private void sendControlCommand(Command command) {
            mpdPlayer.sendControlCommands(Arrays.asList(command), new ResponseReceiver<ResponseResult>() {
                @Override
                public void receiveResponse(ResponseResult response) {
                }
            });
        }
    }
}
