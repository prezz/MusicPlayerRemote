package net.prezz.mpr.ui.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import net.prezz.mpr.R;

public class ThemeHelper {

    private ThemeHelper() {
        //prevent instantiation
    }

    public static boolean applyTheme(Activity activity) {
        if (useDarkTheme(activity)) {
            activity.setTheme(R.style.AppThemeDark);
            return true;
        } else {
            return false;
        }
    }

    public static boolean useDarkTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dark = sharedPreferences.getBoolean(context.getString(R.string.settings_interface_dark_theme_key), false);

        return dark;
    }
}
