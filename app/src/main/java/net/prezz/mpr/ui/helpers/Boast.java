package net.prezz.mpr.ui.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.widget.Toast;

public class Boast {

    private static Object lock = new Object();
    private static Boast lastBoast = null;

    private Toast toast;

    private Boast(Toast toast) {
        if (toast == null) {
            throw new NullPointerException("Boast.Boast(Toast) requires a non-null parameter.");
        }

        this.toast = toast;
    }

    public static Boast makeText(Context context, CharSequence text) {
        return new Boast(Toast.makeText(context, text, Toast.LENGTH_SHORT));
    }

    public static Boast makeText(Context context, int resId) throws Resources.NotFoundException {
        return new Boast(Toast.makeText(context, resId, Toast.LENGTH_SHORT));
    }

//    public void setGravity(int gravity, int xOffset, int yOffset) {
//        toast.setGravity(gravity, xOffset, yOffset);
//    }

    public void cancel() {
        toast.cancel();
    }

    public void show() {
        show(true);
    }

    public void show(boolean cancelCurrent) {
        synchronized (lock) {
            if (cancelCurrent) {
                if (lastBoast != null) {
                    lastBoast.cancel();
                }
            }
            lastBoast = this;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                toast.setMargin(toast.getHorizontalMargin(), 0.05f);
            }
            toast.show();
        }
    }
}