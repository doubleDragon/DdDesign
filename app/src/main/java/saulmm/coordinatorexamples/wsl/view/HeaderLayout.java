package saulmm.coordinatorexamples.wsl.view;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-19.
 */

public class HeaderLayout extends LinearLayout {

    private static final String TAG = HeaderLayout.class.getSimpleName();

    private View firstView;
    private View secondView;
    private View thirdView;

    private int mLastTouchX;
    private int mLastTouchY;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mScrollPointerId;

    public HeaderLayout(Context context) {
        super(context);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final int action = MotionEventCompat.getActionMasked(e);
        final int actionIndex = MotionEventCompat.getActionIndex(e);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollPointerId = MotionEventCompat.getPointerId(e, 0);
                mInitialTouchX = mLastTouchX = (int) (e.getX() + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (e.getY() + 0.5f);
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                mScrollPointerId = MotionEventCompat.getPointerId(e, actionIndex);
                mInitialTouchX = mLastTouchX = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                mInitialTouchY = mLastTouchY = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
                break;
            case MotionEvent.ACTION_MOVE:
                final int index = MotionEventCompat.findPointerIndex(e, mScrollPointerId);
                if (index < 0) {
                    Log.e(TAG, "Error processing scroll; pointer index for id " +
                            mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                    return false;
                }
                final int x = (int) (MotionEventCompat.getX(e, index) + 0.5f);
                final int y = (int) (MotionEventCompat.getY(e, index) + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onPointerUp(e);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

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
}