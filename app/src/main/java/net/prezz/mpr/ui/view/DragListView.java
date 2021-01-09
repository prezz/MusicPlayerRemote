package net.prezz.mpr.ui.view;


import net.prezz.mpr.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;


public class DragListView extends ListView {

    private ImageView dragView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams windowParams;
    private int dragPos; // which item is being dragged
    private int srcDragPos; // where was the dragged item originally
    private int dragPointX;
    private int dragPointY; // at what offset inside the item did the user grab it
    private int xOffset;
    private int yOffset; // the difference between screen coordinates and coordinates in this view
    private DragListener dragListener;
    private DropListener dropListener;
    private RemoveListener removeListener;
    private int upperBound;
    private int lowerBound;
    private int height;
    private Rect tempRect = new Rect();
    private Bitmap dragBitmap;
    private final int touchSlop;
    private int itemHeightNormal;
    private int itemHeightExpanded;
    private int itemHeightHalf;
    private boolean draggingEnabled;
    private boolean deleting;


    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        draggingEnabled = true;
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
//        Resources res = getResources();
//        itemHeightNormal = res.getDimensionPixelSize(R.dimen.view_list_item_height);
//        itemHeightHalf = itemHeightNormal / 2;
//        itemHeightExpanded = itemHeightNormal * 2;
    }

    private boolean isDragItem(View item){
        return (draggingEnabled && item.findViewById(R.id.playlist_list_item_drag_image) != null);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (draggingEnabled && dragListener != null || dropListener != null) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                deleting = false;
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                int itemnum = pointToPosition(x, y);
                if (itemnum == AdapterView.INVALID_POSITION) {
                    break;
                }
                ViewGroup item = (ViewGroup) getChildAt(itemnum - getFirstVisiblePosition());
                itemHeightNormal = item.getHeight();
                itemHeightHalf = itemHeightNormal / 2;
                itemHeightExpanded = itemHeightNormal * 2;
                dragPointX = x - item.getLeft();
                dragPointY = y - item.getTop();
                xOffset = ((int)ev.getRawX()) - x;
                yOffset = ((int)ev.getRawY()) - y;
                View dragger = item.findViewById(R.id.playlist_list_item_drag_image);
                if (!isDragItem(item))
                    return super.onInterceptTouchEvent(ev);

                Rect r = tempRect;
                dragger.getDrawingRect(r);
//                Drag icon is supposed to be in the left, so if we touch left of the drag icons right side we are dragging.
                if (x < r.right) {
                    //enable drawing cache to allow us to create a bitmap of the item we are dragging
                    item.setDrawingCacheEnabled(true);
                    Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
                    item.setDrawingCacheEnabled(false);
                    startDragging(bitmap, x, y);
                    dragPos = itemnum;
                    srcDragPos = dragPos;
                    height = getHeight();
                    upperBound = Math.min(y - touchSlop, height / 3);
                    lowerBound = Math.max(y + touchSlop, height * 2 / 3);
                    return false;
                }
                stopDragging();
                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    /*
     * pointToPosition() doesn't consider invisible views, but we need to, so implement a slightly different version.
     */
    private int myPointToPosition(int x, int y) {

        if (y < 0) {
            // when dragging off the top of the screen, calculate position
            // by going back from a visible item
            int pos = myPointToPosition(x, y + itemHeightNormal);
            if (pos > 0) {
                return pos - 1;
            }
        }

        Rect frame = tempRect;
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            child.getHitRect(frame);
            if (frame.contains(x, y)) {
                return getFirstVisiblePosition() + i;
            }
        }
        return INVALID_POSITION;
    }

    private int getItemForPosition(int y) {
        int adjustedy = y - dragPointY - itemHeightHalf;
        int pos = myPointToPosition(0, adjustedy);
        if (pos >= 0) {
            if (pos <= srcDragPos) {
                pos += 1;
            }
        } else if (adjustedy < 0) {
            // this shouldn't happen anymore now that myPointToPosition deals
            // with this situation
            pos = 0;
        }
        return pos;
    }

    private void adjustScrollBounds(int y) {
        if (y >= height / 3) {
            upperBound = height / 3;
        }
        if (y <= height * 2 / 3) {
            lowerBound = height * 2 / 3;
        }
    }

    /*
     * Restore size and visibility for all listitems
     */
    private void unExpandViews(boolean deletion) {
        //find information necessary to adjust list position if the collapsed
        //source position is scrolled out beyond the top. This is to prevent
        //annoying jump of items when dropping.
        int firstVisibleIndex = getFirstVisiblePosition();
        View firstVisibleChild = getChildAt(0);
        int firstVisibleTop = (firstVisibleChild == null) ? 0 : firstVisibleChild.getTop();
        boolean counterScroll = srcDragPos < firstVisibleIndex;

        for (int i = 0;; i++) {
            View v = getChildAt(i);
            if (v == null) {
                if (deletion) {
                    // HACK force update of mItemCount
                    int position = getFirstVisiblePosition();
                    int y = getChildAt(0).getTop();
                    setAdapter(getAdapter());
                    setSelectionFromTop(position, y);
                    // end hack
                }
                try {
                    layoutChildren(); // force children to be recreated where needed
                    v = getChildAt(i);
                } catch (IllegalStateException ex) {
                    // layoutChildren throws this sometimes, presumably because we're
                    // in the process of being torn down but are still getting touch
                    // events
                }
                if (v == null) {
                    break;
                }
            }
            if (isDragItem(v)){
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.height = itemHeightNormal;
                v.setLayoutParams(params);
                v.setVisibility(View.VISIBLE);
            }
        }

        if (counterScroll) {
            setSelectionFromTop(firstVisibleIndex - 1, firstVisibleTop);
        }
    }

    /*
     * Adjust visibility and size to make it appear as though an item is being dragged around and other items are making room for it: If
     * dropping the item would result in it still being in the same place, then make the dragged listitem's size normal, but make the item
     * invisible. Otherwise, if the dragged listitem is still on screen, make it as small as possible and expand the item below the insert
     * point. If the dragged item is not on screen, only expand the item below the current insertpoint.
     */
    private void doExpansion() {
        int childnum = dragPos - getFirstVisiblePosition();
        if (dragPos > srcDragPos) {
            childnum++;
        }
        int numheaders = getHeaderViewsCount();

        View first = getChildAt(srcDragPos - getFirstVisiblePosition());
        for (int i = 0;; i++) {
            View vv = getChildAt(i);
            if (vv == null) {
                break;
            }

            int height = itemHeightNormal;
            int visibility = View.VISIBLE;
            if (dragPos < numheaders && i == numheaders) {
                // dragging on top of the header item, so adjust the item below
                // instead
                if (vv.equals(first)) {
                    visibility = View.INVISIBLE;
                } else {
                    height = itemHeightExpanded;
                }
            } else if (vv.equals(first)) {
                // processing the item that is being dragged
                if (dragPos == srcDragPos    || getPositionForView(vv) == getCount() - 1) {
                    // hovering over the original location
                    visibility = View.INVISIBLE;
                } else {
                    // not hovering over it
                    // Ideally the item would be completely gone, but neither
                    // setting its size to 0 nor settings visibility to GONE
                    // has the desired effect.
                    height = 1;
                }
            } else if (i == childnum) {
                if (dragPos >= numheaders && dragPos < getCount() - 1) {
                    height = itemHeightExpanded;
                }
            }
            ViewGroup.LayoutParams params = vv.getLayoutParams();
            params.height = height;
            vv.setLayoutParams(params);
            vv.setVisibility(visibility);
        }
    }

    private void collapseDeletionItem() {
        int childnum = srcDragPos - getFirstVisiblePosition();
        View v = getChildAt(childnum);
        if (v == null) {
            return;
        }

        if (isDragItem(v)){
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = 1;
            v.setLayoutParams(params);
            v.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if ((dragListener != null || dropListener != null) && dragView != null) {
            int action = ev.getAction();
            switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Rect r = tempRect;
                dragView.getDrawingRect(r);
                stopDragging();
                if (deleting) {
                    if (removeListener != null) {
                        removeListener.remove(srcDragPos);
                    }
                    unExpandViews(true);
                } else {
                    if (dropListener != null && dragPos >= 0 && dragPos < getCount()) {
                        dropListener.drop(srcDragPos, dragPos);
                    }
                    unExpandViews(false);
                }
                break;

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (!deleting && ev.getX() > tempRect.right * 3 / 4) {
                    if (dragPos != srcDragPos) {
                        unExpandViews(false);
                    }
                    collapseDeletionItem();
                    deleting = true;
                } else if (deleting && (ev.getX() <= tempRect.right * 3 / 4)) {
                    doExpansion();
                    deleting = false;
                }

                int x = (int) ev.getX();
                int y = (int) ev.getY();
                dragView(x, y);
                int itemnum = getItemForPosition(y);
                if (itemnum >= 0) {
                    if (action == MotionEvent.ACTION_DOWN || itemnum != dragPos) {
                        if (dragListener != null) {
                            dragListener.drag(dragPos, itemnum);
                        }
                        dragPos = itemnum;
                        if (!deleting) {
                            doExpansion();
                        }
                    }
                    int speed = 0;
                    adjustScrollBounds(y);
                    if (y > lowerBound) {

                        //if dragging an item down past the point where list begins to scroll so its source position
                        //exists the screen, expand the item on scroll the list one position forward.
                        //This is to fix a bug where some items would not get unexpanded again
                        View v = getChildAt(0);
                        ViewGroup.LayoutParams params = v.getLayoutParams();
                        if (params.height == 1) {
                            params.height = itemHeightNormal;
                            v.setLayoutParams(params);
                            v.setVisibility(View.VISIBLE);
                            int pos = getFirstVisiblePosition();
                            setSelectionFromTop(pos + 1, 0);
                        }

                        // scroll the list up a bit
                        if (getLastVisiblePosition() < getCount() - 1) {
                            speed = y > (height + lowerBound) / 2 ? 16 : 4;
                        } else {
                            speed = 1;
                        }
                    } else if (y < upperBound) {
                        // scroll the list down a bit
                        speed = y < upperBound / 2 ? -16 : -4;
                        if (getFirstVisiblePosition() == 0    && getChildAt(0).getTop() >= getPaddingTop()) {
                            // if we're already at the top, don't try to scroll,
                            // because
                            // it causes the framework to do some extra drawing
                            // that messes
                            // up our animation
                            speed = 0;
                        }
                    }
                    if (speed != 0) {
                        smoothScrollBy(speed, 30);
                    }
                }
                break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

    private void startDragging(Bitmap bm, int x, int y) {
        stopDragging();

        windowParams = new WindowManager.LayoutParams();
        windowParams.gravity = Gravity.TOP | Gravity.LEFT;
        windowParams.x = x - dragPointX + xOffset;
        windowParams.y = y - dragPointY + yOffset;

        windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        windowParams.format = PixelFormat.TRANSLUCENT;
        windowParams.windowAnimations = 0;

        Context context = getContext();
        ImageView v = new ImageView(context);
        int backGroundColor = getDragBackgroundColor();
        v.setBackgroundColor(backGroundColor);
        v.setPadding(0, 0, 0, 0);
        v.setImageBitmap(bm);
        dragBitmap = bm;

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(v, windowParams);
        dragView = v;
    }

    private void dragView(int x, int y) {
        float alpha = 1.0f;
        int width = dragView.getWidth();
        if (x > width / 2) {
            alpha = ((float) (width - x)) / (width / 2);
        }
        windowParams.alpha = alpha;

        windowParams.x = x - dragPointX + xOffset;
        windowParams.y = y - dragPointY + yOffset;
        windowManager.updateViewLayout(dragView, windowParams);
    }

    private void stopDragging() {
        if (dragView != null) {
            dragView.setVisibility(GONE);
            WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(dragView);
            dragView.setImageDrawable(null);
            dragView = null;
        }
        if (dragBitmap != null) {
            dragBitmap.recycle();
            dragBitmap = null;
        }
    }

    public void setDraggingEnabled(boolean enable) {
        draggingEnabled = enable;
    }

    public void setDragListener(DragListener l) {
        dragListener = l;
    }

    public void setDropListener(DropListener l) {
        dropListener = l;
    }

    public void setRemoveListener(RemoveListener l) {
        removeListener = l;
    }

    public interface DragListener {
        void drag(int from, int to);
    }

    public interface DropListener {
        void drop(int from, int to);
    }

    public interface RemoveListener {
        void remove(int which);
    }

    private int getDragBackgroundColor() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.playlistDragColor, typedValue, true);
        return typedValue.data;
    }
}
