package com.wsl.library.design;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

/**
 * Created by wsl on 16-8-23.
 */

abstract class DdHeaderBehavior<V extends View> extends ViewOffsetBehavior<V> {

    /**
     * 第一页非置底
     * Stable child指TabLayout
     */
    public static final int SCROLL_STATE_STABLE_CHILD_INVISIBLE = 0;
    /**
     * 第一页置底
     */
    public static final int SCROLL_STATE_STABLE_CHILD_READY = 1;

    /**
     * 1和3的中间状态
     */
    public static final int SCROLL_STATE_STABLE_CHILD_MIDDLE = 2;

    /**
     * 第二页置顶
     */
    public static final int SCROLL_STATE_STABLE_CHILD_TOP = 3;

    public static final int DEFAULT_AUTO_SCROLL_DURATION = 500;


    private static final int INVALID_POINTER = -1;

    private Runnable mFlingRunnable;
    private Runnable mAutoScrollRunnable;
    private ScrollerCompat mScroller;
    private ScrollerCompat mAutoScroller;

    private boolean mIsBeingDragged;
    private int mActivePointerId = INVALID_POINTER;
    private int mLastMotionY;
    private int mTouchSlop = -1;
    private VelocityTracker mVelocityTracker;

    private int mScrollState = SCROLL_STATE_STABLE_CHILD_INVISIBLE;

    public DdHeaderBehavior() {
    }

    public DdHeaderBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }

        final int action = ev.getAction();

        // Shortcut since we're being dragged
        if (action == MotionEvent.ACTION_MOVE && mIsBeingDragged) {
            return true;
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                mIsBeingDragged = false;
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                if (canDragView(child) && parent.isPointInChildBounds(child, x, y)) {
                    mLastMotionY = y;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    ensureVelocityTracker();
                    abortAutoScroll(child);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
                final int yDiff = Math.abs(y - mLastMotionY);
                if (yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent ev) {
        if (mTouchSlop < 0) {
            mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
        }
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (parent.isPointInChildBounds(child, x, y) && canDragView(child)) {
                    mLastMotionY = y;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    ensureVelocityTracker();
                } else {
                    return false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                        mActivePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                int dy = mLastMotionY - y;

                if (!mIsBeingDragged && Math.abs(dy) > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (dy > 0) {
                        dy -= mTouchSlop;
                    } else {
                        dy += mTouchSlop;
                    }
                }

                if (mIsBeingDragged) {
                    mLastMotionY = y;
                    // We're being dragged so scroll the ABL
                    scroll(parent, child, dy, getMaxDragOffset(child), 0);
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float yvel = VelocityTrackerCompat.getYVelocity(mVelocityTracker,
                            mActivePointerId);

                    if(!isReadyEnabled(child)) {
                        // 回弹效果关闭, 直接fling
                        fling(parent, child, -getScrollRangeForDragFling(child), 0, yvel);
                    } else {
                        boolean needAutoScroll = false;
                        switch (mScrollState) {
                            case SCROLL_STATE_STABLE_CHILD_INVISIBLE:
                                fling(parent, child, -getOffsetWhenReadyState(child), 0, yvel);
                                break;
                            case SCROLL_STATE_STABLE_CHILD_READY:
                                if (!isScrollUpWhenReady(child)) {
                                    fling(parent, child, -getOffsetWhenReadyState(child), 0, yvel);
                                } else {
                                    needAutoScroll = true;
                                }
                                break;
                            case SCROLL_STATE_STABLE_CHILD_MIDDLE:
                                needAutoScroll = true;
                                break;
                            case SCROLL_STATE_STABLE_CHILD_TOP:
                                if (!isScrollUpWhenTop(child)) {
                                    //visible状态下向下滑动时auto scroll
                                    needAutoScroll = true;
                                }
                                break;
                        }
                        if (needAutoScroll) {
                            int dy;
                            if (yvel > 0) {
                                //We're scrolling down, need auto scroll to ready state
                                dy = -getTopBottomOffsetForScrollingSibling() - getOffsetWhenReadyState(child);
                            } else {
                                //We're scrolling up, need auto scroll to top state
                                dy = -getTopBottomOffsetForScrollingSibling() - getOffsetWhenTopState(child);
                            }
                            autoScroll(parent, child, dy);
                        }
                    }
                }
                // $FALLTHROUGH
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

                break;
            }
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }

        return true;
    }

    boolean isBeingDragged() {
        return mIsBeingDragged;
    }

    boolean isReadyEnabled(V view) {
        //默认开启回弹效果
        return true;
    }

    boolean isScrollUpWhenReady(V view) {
        return false;
    }

    boolean isScrollUpWhenTop(V view) {
        return false;
    }

    void setScrollState(int scrollState) {
        if (this.mScrollState == scrollState) {
            return;
        }
        this.mScrollState = scrollState;
    }

    int getScrollState() {
        return this.mScrollState;
    }

    int getOffsetWhenReadyState(V header) {
        return header.getHeight();
    }

    int getOffsetWhenTopState(V header) {
        return header.getHeight();
    }

    int setHeaderTopBottomOffset(CoordinatorLayout parent, V header, int newOffset) {
        return setHeaderTopBottomOffset(parent, header, newOffset,
                Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    int setHeaderTopBottomOffset(CoordinatorLayout parent, V header, int newOffset,
                                 int minOffset, int maxOffset) {
        final int curOffset = getTopAndBottomOffset();
        int consumed = 0;

        if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
            // If we have some scrolling range, and we're currently within the min and max
            // offsets, calculate a new offset
            newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);

            if (curOffset != newOffset) {
                setTopAndBottomOffset(newOffset);
                // Update how much dy we have consumed
                consumed = curOffset - newOffset;
            }
        }

        return consumed;
    }

    int getTopBottomOffsetForScrollingSibling() {
        return getTopAndBottomOffset();
    }

    final int scroll(CoordinatorLayout coordinatorLayout, V header,
                     int dy, int minOffset, int maxOffset) {
        return setHeaderTopBottomOffset(coordinatorLayout, header,
                getTopBottomOffsetForScrollingSibling() - dy, minOffset, maxOffset);
    }

    final boolean autoScroll(CoordinatorLayout coordinatorLayout, V layout, int dy) {
        return autoScroll(coordinatorLayout, layout, dy, DEFAULT_AUTO_SCROLL_DURATION);
    }

    final boolean autoScroll(CoordinatorLayout coordinatorLayout, V layout, int dy, int duration) {
        if (mAutoScrollRunnable != null) {
            layout.removeCallbacks(mAutoScrollRunnable);
            mAutoScrollRunnable = null;
        }

        if (mAutoScroller == null) {
            mAutoScroller = ScrollerCompat.create(layout.getContext());
        }
        mAutoScroller.startScroll(0, getTopAndBottomOffset(), 0, dy, duration);
        if (mAutoScroller.computeScrollOffset()) {
            mAutoScrollRunnable = new DdHeaderBehavior.AutoScrollRunnable(coordinatorLayout, layout);
            ViewCompat.postOnAnimation(layout, mAutoScrollRunnable);
            return true;
        }
        return false;
    }

    final void abortAutoScroll(V layout) {
        if (mAutoScrollRunnable != null) {
            layout.removeCallbacks(mAutoScrollRunnable);
            mAutoScrollRunnable = null;
        }
        if(mAutoScroller != null) {
            if(!mAutoScroller.isFinished()) {
//                mAutoScroller.forceFinished(true);
                mAutoScroller.abortAnimation();
            }
        }
        if (mFlingRunnable != null) {
            layout.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }
        if(mScroller != null) {
            if(mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
        }
    }

    final boolean fling(CoordinatorLayout coordinatorLayout, V layout, int minOffset,
                        int maxOffset, float velocityY) {
        if (mFlingRunnable != null) {
            layout.removeCallbacks(mFlingRunnable);
            mFlingRunnable = null;
        }

        if (mScroller == null) {
            mScroller = ScrollerCompat.create(layout.getContext());
        }

        mScroller.fling(
                0, getTopAndBottomOffset(), // curr
                0, Math.round(velocityY), // velocity.
                0, 0, // x
                minOffset, maxOffset); // y


        if (mScroller.computeScrollOffset()) {
            mFlingRunnable = new DdHeaderBehavior.FlingRunnable(coordinatorLayout, layout);
            ViewCompat.postOnAnimation(layout, mFlingRunnable);
            return true;
        }
        return false;
    }

    /**
     * Return true if the view can be dragged.
     */
    boolean canDragView(V view) {
        return false;
    }

    /**
     * Returns the maximum px offset when {@code view} is being dragged.
     */
    int getMaxDragOffset(V view) {
        return -view.getHeight();
    }

    int getScrollRangeForDragFling(V view) {
        return view.getHeight();
    }

    private void ensureVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private class FlingRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final V mLayout;

        FlingRunnable(CoordinatorLayout parent, V layout) {
            mParent = parent;
            mLayout = layout;
        }

        @Override
        public void run() {
            if (mLayout != null && mScroller != null && mScroller.computeScrollOffset()) {
                setHeaderTopBottomOffset(mParent, mLayout, mScroller.getCurrY());

                // Post ourselves so that we run on the next animation
                ViewCompat.postOnAnimation(mLayout, this);
            }
        }
    }

    private class AutoScrollRunnable implements Runnable {
        private final CoordinatorLayout mParent;
        private final V mLayout;

        AutoScrollRunnable(CoordinatorLayout parent, V layout) {
            mParent = parent;
            mLayout = layout;
        }

        @Override
        public void run() {
            if (mLayout != null && mAutoScroller != null && mAutoScroller.computeScrollOffset()) {
                setHeaderTopBottomOffset(mParent, mLayout, mAutoScroller.getCurrY());

                // Post ourselves so that we run on the next animation
                ViewCompat.postOnAnimation(mLayout, this);
            }
        }
    }
}
