package net.prezz.mpr.ui.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;

import net.prezz.mpr.R;
import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.ResponseReceiver;
import net.prezz.mpr.model.ResponseResult;
import net.prezz.mpr.model.command.VolumeDownCommand;
import net.prezz.mpr.model.command.VolumeUpCommand;
import net.prezz.mpr.service.StreamingService;

public class VolumeButtonsHelper {

    private VolumeButtonsHelper() {
    }

    public static boolean handleKeyDown(final Context context, int keyCode, KeyEvent event) {

        if (!StreamingService.isStarted()) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            Resources resources = context.getResources();
            boolean enabled = sharedPreferences.getBoolean(resources.getString(R.string.settings_behavior_override_volume_buttons_key), true);

            if (enabled) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        MusicPlayerControl.sendControlCommand(new VolumeUpCommand(getVolumeAmount(context)), new ResponseReceiver<ResponseResult>() {
                            @Override
                            public void receiveResponse(ResponseResult response) {
                                if (response.isSuccess()) {
                                    Integer volume = (Integer) response.getResponseValue(ResponseResult.ValueType.VOLUME);
                                    if (volume != null) {
                                        String text = (volume.intValue() != -1) ? context.getString(R.string.general_volume_text_format, volume) : context.getString(R.string.general_volume_text_no_mixer);
                                        Boast toast = Boast.makeText(context, text);
                                        toast.show();
                                    }
                                }
                            }
                        });
                        return true;

                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        MusicPlayerControl.sendControlCommand(new VolumeDownCommand(getVolumeAmount(context)), new ResponseReceiver<ResponseResult>() {
                            @Override
                            public void receiveResponse(ResponseResult response) {
                                if (response.isSuccess()) {
                                    Integer volume = (Integer) response.getResponseValue(ResponseResult.ValueType.VOLUME);
                                    if (volume != null) {
                                        String text = (volume.intValue() != -1) ? context.getString(R.string.general_volume_text_format, volume) : context.getString(R.string.general_volume_text_no_mixer);
                                        Boast toast = Boast.makeText(context, text);
                                        toast.show();
                                    }
                                }
                            }
                        });
                        return true;
                }
            }
        }

        return false;
    }

    public static int getVolumeAmount(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        return Integer.parseInt(sharedPreferences.getString(resources.getString(R.string.settings_volume_control_amount_key), "1"));
    }
}
