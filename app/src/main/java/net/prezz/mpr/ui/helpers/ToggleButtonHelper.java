package net.prezz.mpr.ui.helpers;

import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.TypedValue;
import android.widget.ImageButton;

import net.prezz.mpr.R;

public class ToggleButtonHelper {

    private ToggleButtonHelper() {
    }

    public static void toggleButton(Activity activity, ImageButton button, boolean toggled) {
        int attr = (toggled) ? R.attr.redFocusColor :  R.attr.iconColor;

        TypedValue typedValue = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedValue, true);
        ColorFilter colorFilter = new PorterDuffColorFilter(typedValue.data, PorterDuff.Mode.SRC_IN);
        button.setColorFilter(colorFilter);
    }
}
