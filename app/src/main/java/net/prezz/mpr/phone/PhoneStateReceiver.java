package net.prezz.mpr.phone;

import net.prezz.mpr.model.MusicPlayerControl;
import net.prezz.mpr.model.command.PauseCommand;
import net.prezz.mpr.R;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import androidx.preference.PreferenceManager;

public class PhoneStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (pauseOnIncommingCall(context) && hasPhoneStatePermission(context)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String state = bundle.getString(TelephonyManager.EXTRA_STATE);
                boolean shouldPause = TelephonyManager.EXTRA_STATE_RINGING.equalsIgnoreCase(state) || TelephonyManager.EXTRA_STATE_OFFHOOK.equalsIgnoreCase(state);
                if (shouldPause) {
                    MusicPlayerControl.sendControlCommand(new PauseCommand(false));
                }
            }
        }
    }

    private boolean pauseOnIncommingCall(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();
        return sharedPreferences.getBoolean(resources.getString(R.string.settings_behavior_pause_on_phonecall_key), false);
    }

    private boolean hasPhoneStatePermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }
}
