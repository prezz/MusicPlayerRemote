package net.prezz.mpr.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SwipeToggleViewPager extends ViewPager {

	private boolean swipeEnabled = true;
	
    public SwipeToggleViewPager(Context context) {
        super(context);
    }

    public SwipeToggleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipeEnabled ? super.onInterceptTouchEvent(event) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeEnabled ? super.onTouchEvent(event) : false;
    }
    
    public void setSwipeEnabled(boolean enabled) {
    	this.swipeEnabled = enabled;
    }
}