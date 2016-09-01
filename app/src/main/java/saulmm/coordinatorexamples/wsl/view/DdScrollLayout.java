package saulmm.coordinatorexamples.wsl.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

/**
 * Created by wsl on 16-8-19.
 */

public class DdScrollLayout extends ViewGroup {

    private static final String TAG = DdScrollLayout.class.getSimpleName();

    private int mTouchSlop;
    private int mLastTouchX;
    private int mLastTouchY;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mScrollPointerId;


    public DdScrollLayout(Context context) {
        this(context, null);
    }

    public DdScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        int count = getChildCount();
//        if (count != 2) {
//            throw new IllegalArgumentException("must exists two child view");
//        }
//    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        int maxHeight = 0;
        int childState = 0;

        //measure content
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            maxWidth = Math.max(maxWidth, childWidth + lp.leftMargin + lp.rightMargin);
            maxHeight += childHeight + lp.topMargin + lp.bottomMargin;
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //get the available size of child view
        final int parentLeft = this.getPaddingLeft();
        final int parentRight = r - l - this.getPaddingRight();
        final int parentTop = this.getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

        int childHeightSum = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            int left = parentLeft + lp.leftMargin;
            int top = parentTop + lp.topMargin + childHeightSum;
            int right = Math.min(left + childWidth, parentRight - lp.rightMargin);
            int bottom = top + childHeight;

            childHeightSum += childHeight;

            child.layout(left, top, right, bottom);

            if(i == count - 1) {
                //set last child height to min height
                setMinimumHeight(childHeight + lp.topMargin + lp.bottomMargin);
            }
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent e) {
//        Log.d("event", "onTouchEvent : " + dumpEvent(e));
//        final int action = MotionEventCompat.getActionMasked(e);
//        final int actionIndex = MotionEventCompat.getActionIndex(e);
//        switch (action) {
//            case MotionEvent.ACTION_DOWN:
//                mScrollPointerId = MotionEventCompat.getPointerId(e, 0);
//                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
//                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);
//                break;
//            case MotionEventCompat.ACTION_POINTER_DOWN:
//                mScrollPointerId = MotionEventCompat.getPointerId(e, actionIndex);
//                mInitialTouchX = mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
//                mInitialTouchY = mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
//                break;
//            case MotionEvent.ACTION_MOVE:
//                final int index = MotionEventCompat.findPointerIndex(e, mScrollPointerId);
//                if (index < 0) {
//                    Log.e(TAG, "Error processing scroll; pointer index for id " +
//                            mScrollPointerId + " not found. Did any MotionEvents get skipped?");
//                    return false;
//                }
//                final int x = (int) (MotionEventCompat.getX(e, index) + 0.5f);
//                final int y = (int) (MotionEventCompat.getY(e, index) + 0.5f);
//                int dx = mLastTouchX - x;
//                int dy = mLastTouchY - y;
//                if (Math.abs(dy) > mTouchSlop) {
//                    if (dy > 0) {
//                        dy -= mTouchSlop;
//                    } else {
//                        dy += mTouchSlop;
//                    }
//                    mLastTouchX = x;
//                    mLastTouchY = y;
//                    //scroll dy
//                    scroll(-dy);
////                    scrollBy(0, dy);
//                }
//                break;
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onPointerUp(e);
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                break;
//        }
//        return true;
//    }

    private void onPointerUp(MotionEvent e) {
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        if (MotionEventCompat.getPointerId(e, actionIndex) == mScrollPointerId) {
            // Pick a new pointer to pick up the slack.
            final int newIndex = actionIndex == 0 ? 1 : 0;
            mScrollPointerId = MotionEventCompat.getPointerId(e, newIndex);
            mInitialTouchX = mLastTouchX = (int) (MotionEventCompat.getX(e, newIndex) + 0.5f);
            mInitialTouchY = mLastTouchY = (int) (MotionEventCompat.getY(e, newIndex) + 0.5f);
        }
    }

    private void scroll(int offset) {
        Log.d("test", "scroll :" + offset);
        Log.d("scroll", "scrollY before: " + getScrollY());
        scrollBy(0, offset);
        Log.d("scroll", "scrollY after: " + getScrollY());
//        offsetTopAndBottom(offset);
//        int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            View child = getChildAt(i);
//            if (child.getVisibility() == View.GONE) {
//                continue;
//            }
////            child.offsetTopAndBottom(offset);
//            ViewCompat.offsetTopAndBottom(child, offset);
//        }
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    private String dumpEvent(MotionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getAction());
        sb.append("[").append(event.getX()).append(",").append(event.getY()).append("]");
        return sb.toString();
    }
}