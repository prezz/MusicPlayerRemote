package net.prezz.mpr.ui.view;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListView;


/**
 * On Android KitKat (4.4.x) the fastscroll slider is not always visible due to some bug in Android.
 * This class is a hack to fix this issue
 */
public class KitKatFixedListView extends ListView {

    public KitKatFixedListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        fixFastscroll();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void fixFastscroll() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setOnScrollListener(new OnScrollListener() {

                private AbsListView view;
                private Handler handler = new Handler();
                private Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        view.setFastScrollAlwaysVisible(false);
                        view = null;
                    }
                };

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    if (scrollState == SCROLL_STATE_IDLE) {
                        this.view = view;
                        handler.postDelayed(runnable, 1000);
                    } else {
                        view.setFastScrollAlwaysVisible(true);
                        handler.removeCallbacks(runnable);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });
        }
    }
}
