package net.prezz.mpr.widget;

import java.util.Arrays;

import net.prezz.mpr.model.PlayerState;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.command.Command;
import net.prezz.mpr.model.command.NextCommand;
import net.prezz.mpr.model.command.PlayPauseCommand;
import net.prezz.mpr.model.command.VolumeDownCommand;
import net.prezz.mpr.model.command.VolumeUpCommand;
import net.prezz.mpr.mpd.MpdPlayer;
import net.prezz.mpr.service.PlaybackService;
import net.prezz.mpr.ui.helpers.Boast;
import net.prezz.mpr.ui.helpers.VolumeButtonsHelper;
import net.prezz.mpr.ui.mpd.MpdPlayerSettings;
import net.prezz.mpr.ui.player.PlayerActivity;
import net.prezz.mpr.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class RemoteWidgetProvider extends AppWidgetProvider {
    
	private static final String CMD_VOL_DOWN = "net.prezz.mpr.widget.RemoteWidgetProvider.CMD_VOL_DOWN";
	private static final String CMD_VOL_UP = "net.prezz.mpr.widget.RemoteWidgetProvider.CMD_VOL_UP";
	private static final String CMD_PLAY_PAUSE = "net.prezz.mpr.widget.RemoteWidgetProvider.CMD_PLAY_PAUSE";
	private static final String CMD_NEXT = "net.prezz.mpr.widget.RemoteWidgetProvider.CMD_NEXT";
	

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	for (int i = 0; i < appWidgetIds.length; i++) {
	        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_remote);
	
	        Intent launchIntent = new Intent(context, PlayerActivity.class);
	    	views.setOnClickPendingIntent(R.id.widget_button_launch, PendingIntent.getActivity(context, 0, launchIntent, 0));
	    	
	    	Intent volDownIntent = new Intent(context, RemoteWidgetProvider.class);
	    	volDownIntent.setAction(CMD_VOL_DOWN);
	    	views.setOnClickPendingIntent(R.id.widget_button_volume_down, PendingIntent.getBroadcast(context, 0, volDownIntent, 0));
	    	
	        Intent volUpIntent = new Intent(context, RemoteWidgetProvider.class);
	        volUpIntent.setAction(CMD_VOL_UP);
	        views.setOnClickPendingIntent(R.id.widget_button_volume_up, PendingIntent.getBroadcast(context, 0, volUpIntent, 0));
	
	        Intent playPauseIntent = new Intent(context, RemoteWidgetProvider.class);
	        playPauseIntent.setAction(CMD_PLAY_PAUSE);
	        views.setOnClickPendingIntent(R.id.widget_button_playpause, PendingIntent.getBroadcast(context, 0, playPauseIntent, 0));
	
	        Intent nextIntent = new Intent(context, RemoteWidgetProvider.class);
	        nextIntent.setAction(CMD_NEXT);
	        views.setOnClickPendingIntent(R.id.widget_button_next, PendingIntent.getBroadcast(context, 0, nextIntent, 0));
	        
	        appWidgetManager.updateAppWidget(appWidgetIds[i], views);
    	}
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	super.onReceive(context, intent);
    	
		String action = intent.getAction();
		
		if (CMD_VOL_DOWN.equals(action)) {
			sendControlCommand(context, new VolumeDownCommand(VolumeButtonsHelper.getVolumeAmount(context)), false);
		} else if (CMD_VOL_UP.equals(action)) {
			sendControlCommand(context, new VolumeUpCommand(VolumeButtonsHelper.getVolumeAmount(context)), false);
		} else if (CMD_PLAY_PAUSE.equals(action)) {
			sendControlCommand(context, new PlayPauseCommand(), true);
		} else if (CMD_NEXT.equals(action)) {
			sendControlCommand(context, new NextCommand(), false);
		}
    }
    
    private void sendControlCommand(final Context context, final Command command, final boolean startService) {
        final MpdPlayer mpdPlayer = new MpdPlayer(MpdPlayerSettings.create(context));
		mpdPlayer.sendControlCommands(Arrays.asList(command), new ResponseReceiver<ResponseResult>() {
			@Override
			public void receiveResponse(ResponseResult response) {
				mpdPlayer.dispose();

				if (startService && response.isSuccess()) {
					PlaybackService.start();
				}

                if (response.isSuccess()) {
                    Integer volume = (Integer) response.getResponseValue(ResponseResult.ValueType.VOLUME);
                    if (volume != null) {
                        Boast.makeText(context, context.getString(R.string.general_volume_text_format, volume)).show();
                    }

					PlayerState playerState = (PlayerState) response.getResponseValue(ResponseResult.ValueType.PLAYER_STATE);
					if (playerState != null) {
                        switch (playerState) {
                            case PLAY:
                                Boast.makeText(context, context.getString(R.string.general_player_state_play)).show();
                                break;
                            case STOP:
                                Boast.makeText(context, context.getString(R.string.general_player_state_stop)).show();
                                break;
                            case PAUSE:
                                Boast.makeText(context, context.getString(R.string.general_player_state_pause)).show();
                                break;
                        }
					}
                }
			}
		});
    }
}
