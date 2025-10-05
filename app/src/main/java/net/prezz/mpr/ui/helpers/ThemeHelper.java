package net.prezz.mpr.ui.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;

import net.prezz.mpr.R;

public class ThemeHelper {

    private ThemeHelper() {
        //prevent instantiation
    }

    public static boolean applyTheme(Activity activity) {

        boolean useDarkTheme = useDarkTheme(activity);
        if (useDarkTheme) {
            activity.setTheme(R.style.AppThemeDark);
        }

        Window window = activity.getWindow();
        View view = window.getDecorView();

        WindowCompat.getInsetsController(window, view).setAppearanceLightStatusBars(!useDarkTheme);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insert) -> {
            Insets bars = insert.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        return useDarkTheme;
    }

    public static boolean useDarkTheme(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean dark = sharedPreferences.getBoolean(context.getString(R.string.settings_interface_dark_theme_key), false);

        return dark;
    }
}
