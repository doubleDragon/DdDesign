package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Replace AppBarLayout
 * Created by wsl on 16-8-24.
 */

@CoordinatorLayout.DefaultBehavior(DdHeaderLayout.Behavior.class)
public class DdHeaderLayout extends ViewGroup {

    private static final String TAG = DdHeaderLayout.class.getSimpleName();

    public interface OnOffsetChangedListener {
        void onOffsetChanged(DdHeaderLayout appBarLayout, int verticalOffset);
    }

    private static final int INVALID_SCROLL_RANGE = -1;

    private int mTotalScrollRange = INVALID_SCROLL_RANGE;
    private int mDownPreScrollRange = INVALID_SCROLL_RANGE;
    private int mDownScrollRange = INVALID_SCROLL_RANGE;
    private int mBottomStableChildReadyVisibleScrollRange = INVALID_SCROLL_RANGE;//超出屏幕部分的滑动偏移

    private WindowInsetsCompat mLastInsets;

    private final List<OnOffsetChangedListener> mListeners = new ArrayList<>();

    private int mTopInsetHeight;
    private int mReadyOffset;
    private boolean mReadyEnabled;
    private boolean mConsumeTopInsets;
    private boolean mDebug;

    public DdHeaderLayout(Context context) {
        this(context, null);
    }

    public DdHeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setWillNotDraw(true);


        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdHeaderLayout);
        mReadyOffset = a.getDimensionPixelSize(R.styleable.DdHeaderLayout_dd_ready_offset, 0);
        mConsumeTopInsets = a.getBoolean(R.styleable.DdHeaderLayout_dd_consume_top_insets, true);
        mReadyEnabled = a.getBoolean(R.styleable.DdHeaderLayout_dd_ready_enabled, true);
        a.recycle();

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        mTopInsetHeight = insets != null ? insets.getSystemWindowInsetTop() : 0;
                        if (!mConsumeTopInsets) {
                            return insets;
                        }
                        setWindowInsets(insets);
                        return insets.consumeSystemWindowInsets();
                    }
                });
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    public void addOnOffsetChangedListener(DdHeaderLayout.OnOffsetChangedListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeOnOffsetChangedListener(DdHeaderLayout.OnOffsetChangedListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        if (count != 2) {
            throw new IllegalArgumentException("must exists two child view");
        }
    }

    private void invalidateScrollRanges() {
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mBottomStableChildReadyVisibleScrollRange = INVALID_SCROLL_RANGE;
    }

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
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
            maxHeight += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin - lp.getOverlayTop();
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

//      resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT) return wrong height
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                maxHeight);
        invalidateScrollRanges();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //get the available size of child view
        final int parentLeft = this.getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();
        final int parentTop = this.getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

        int childHeightSum = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            LayoutParams lp = (LayoutParams) child.getLayoutParams();

            int left = parentLeft + lp.leftMargin;
            int right = Math.min(left + childWidth, parentRight - lp.rightMargin);

            int top = parentTop + lp.topMargin - lp.getOverlayTop() + childHeightSum;
            int bottom = top + childHeight;

            childHeightSum += childHeight;

            child.layout(left, top, right, bottom);
        }
    }

    private boolean hasChildWithInterpolator() {
        return false;
    }

    private void setWindowInsets(WindowInsetsCompat insets) {
        // Invalidate the total scroll range...
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mLastInsets = insets;

        // Now dispatch them to our children
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
            if (insets.isConsumed()) {
                break;
            }
        }
    }

    private boolean isReadyEnabled() {
        return mReadyEnabled;
    }

    private int getTopInset() {
        return mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
    }

    private boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    public int getTotalScrollRange() {
        if (mTotalScrollRange != INVALID_SCROLL_RANGE) {
            return mTotalScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final DdHeaderLayout.LayoutParams lp = (DdHeaderLayout.LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();
            // We're set to scroll so add the child's height
            range += childHeight + lp.topMargin + lp.bottomMargin;
            range -= lp.getOverlayTop();//减去覆盖的部分
            //DdCollapsingBarLayout的minHeight表示toolbar的高度
            //DdSecondLayout的minHeight表示TabLayout的高度
            //滑动到顶部的时候最终剩下的就是toolbar和TabLayout
            range -= ViewCompat.getMinimumHeight(child);
        }
        return mTotalScrollRange = Math.max(0, range - getTopInset());
    }

    private int getBottomStableChildReadyVisibleScrollRange() {
        if (mBottomStableChildReadyVisibleScrollRange != INVALID_SCROLL_RANGE) {
            return mBottomStableChildReadyVisibleScrollRange;
        }
        int range = 0;
        int overTotalHeight = getHeight() - DdUtil.getScreenHeight(getContext());
        if(!mConsumeTopInsets) {
            overTotalHeight += mTopInsetHeight;
        }
        int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                range = overTotalHeight - ViewCompat.getMinimumHeight(child);
                break;
            }
        }
        range += mReadyOffset;
        return mBottomStableChildReadyVisibleScrollRange = range;
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

        private int overlayTop;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdHeaderLayout_LayoutParams);
            overlayTop = a.getDimensionPixelSize(R.styleable.DdHeaderLayout_LayoutParams_dd_overlapTop, 0);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.overlayTop = source.overlayTop;
        }

        public void setOverlayTop(int overlayTop) {
            this.overlayTop = overlayTop;
        }

        public int getOverlayTop() {
            return this.overlayTop;
        }
    }


    public static class Behavior extends DdHeaderBehavior<DdHeaderLayout> {

        private static final int INVALID_POSITION = -1;

        private boolean mWasFlung;
        private int mOffsetDelta;

        private boolean mSkipNestedPreScroll;

        private ValueAnimatorCompat mAnimator;

        private int mOffsetToChildIndexOnLayout = INVALID_POSITION;
        private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
        private float mOffsetToChildIndexOnLayoutPerc;

        private WeakReference<View> mLastNestedScrollingChildRef;

        public Behavior() {
            super();
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout parent, DdHeaderLayout child, View directTargetChild, View target, int nestedScrollAxes) {
            // just started nested scroll when target first item display totally
            boolean canScrollDown = !ViewCompat.canScrollVertically(target, -1);

            // Return true if we're nested scrolling vertically, and we have scrollable children
            // and the scrolling view is big enough to scroll
            final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                    && child.hasScrollableChildren()
                    && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight()
                    && canScrollDown;

            if (started && mAnimator != null) {
                // Cancel any offset animation
                mAnimator.cancel();
            }
            //cancel auto scroll
            abortAutoScroll(child);

            // A new nested scroll has started so clear out the previous ref
            mLastNestedScrollingChildRef = null;

            return started;
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, DdHeaderLayout child,
                                      View target, int dx, int dy, int[] consumed) {
            if (dy != 0 && !mSkipNestedPreScroll) {
                int min, max;
                min = -child.getTotalScrollRange();
                max = 0;
                consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
            }
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, DdHeaderLayout child,
                                   View target, int dxConsumed, int dyConsumed,
                                   int dxUnconsumed, int dyUnconsumed) {
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                scroll(coordinatorLayout, child, dyUnconsumed,
                        -child.getTotalScrollRange(), 0);
                // Set the expanding flag so that onNestedPreScroll doesn't handle any events
                mSkipNestedPreScroll = true;
            } else {
                // As we're no longer handling nested scrolls, reset the skip flag
                mSkipNestedPreScroll = false;
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, DdHeaderLayout abl,
                                       View target) {
            if (!mWasFlung && abl.isReadyEnabled()) {
                // If we haven't been flung then let's see if the current view has been set to snap
                // onNestedPreFling　don't invoke, so trigger to top state
                snapToChildIfNeeded(coordinatorLayout, abl);
            }

            // Reset the flags
            mSkipNestedPreScroll = false;
            mWasFlung = false;
            // Keep a reference to the previous nested scrolling child
            mLastNestedScrollingChildRef = new WeakReference<>(target);
        }

        @Override
        public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, DdHeaderLayout child, View target, float velocityX, float velocityY) {
            if(!child.isReadyEnabled()) {
                //如果不需要回弹效果
                if(velocityY < 0) {
                    //手指向下, 被依赖的DdHeaderLayout拦截fling效果,依赖的View不处理
                    fling(coordinatorLayout, child, getTopBottomOffsetForScrollingSibling(), 0, -velocityY);
                    mWasFlung = true;
                    return true;
                } else {
                    //手指向上,
                    // DdHeaderLayout如果没达到最大滑动距离,fling部分距离到最大滑动位置
                    // 如果还剩余fling距离，交给依赖View处理fling
                    if(child.getTotalScrollRange() == -getTopBottomOffsetForScrollingSibling()) {
                        return false;
                    }
                    fling(coordinatorLayout, child, -getScrollRangeForDragFling(child), 0, -velocityY);
                    mWasFlung = true;
                    return true;
                }
            }
            int dy;
            if (velocityY < 0) {
                //(手指向下)We're scrolling down, need auto scroll to ready state
                dy = -getTopBottomOffsetForScrollingSibling() - child.getBottomStableChildReadyVisibleScrollRange();
            } else {
                //(手指向上)We're scrolling up, need auto scroll to top state
                dy = -getTopBottomOffsetForScrollingSibling() - child.getTotalScrollRange();
            }
            autoScroll(coordinatorLayout, child, dy);
            mWasFlung = true;
            return true;
        }

        @Override
        boolean isReadyEnabled(DdHeaderLayout view) {
            return view.isReadyEnabled();
        }

        /**
         * drag　minOff偏移量计算
         *
         * @param view
         * @return
         */
        @Override
        int getMaxDragOffset(DdHeaderLayout view) {
            if (view.isReadyEnabled() && getScrollState() == SCROLL_STATE_STABLE_CHILD_INVISIBLE) {
                return -view.getBottomStableChildReadyVisibleScrollRange();
            }
            return -view.getTotalScrollRange();
        }

        @Override
        int getScrollRangeForDragFling(DdHeaderLayout view) {
            return view.getTotalScrollRange();
        }

        @Override
        int getOffsetWhenReadyState(DdHeaderLayout header) {
            return header.getBottomStableChildReadyVisibleScrollRange();
        }

        @Override
        int getOffsetWhenTopState(DdHeaderLayout header) {
            return header.getTotalScrollRange();
        }

        /**
         * Ready状态下是向上滑动还是向下滑动
         *
         * @return true　向上
         */
        @Override
        boolean isScrollUpWhenReady(DdHeaderLayout view) {
            int stableChildReadyScrollRange = view.getBottomStableChildReadyVisibleScrollRange();
            int scrollSibling = -getTopBottomOffsetForScrollingSibling();
            return scrollSibling > stableChildReadyScrollRange;
        }

        /**
         * Visible状态下是向上滑动还是向下滑动, (按住TabLayout滑动)
         *
         * @return true　向上
         */
        @Override
        boolean isScrollUpWhenTop(DdHeaderLayout view) {
            int scrollSibling = -getTopBottomOffsetForScrollingSibling();
            return scrollSibling >= view.getTotalScrollRange();
        }

        /**
         * Top　or Middle 状态下滑动,根据滑动的距离决定是否回滚
         * 判断标准:滑动超过半屏幕
         *
         * @return true 回滚
         */
        boolean isShouldSnapToOriginWhenVisible(DdHeaderLayout view) {
            int scrollSibling = -getTopBottomOffsetForScrollingSibling();
            int scrollSiblingTop = view.getTotalScrollRange();
            int scrollSiblingReady = view.getBottomStableChildReadyVisibleScrollRange();
            int temp = (scrollSiblingTop - scrollSiblingReady) / 2 + scrollSiblingReady;
            if (scrollSibling > (temp)) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Ready状态向上滑动,根据滑动的距离决定是否回滚
         * 判断标准:滑动超过半屏幕
         *
         * @param view
         * @return
         */
        boolean isShouldSnapToOriginWhenReady(DdHeaderLayout view) {
            int scrollSibling = -getTopBottomOffsetForScrollingSibling();
            int scrollSiblingReady = view.getBottomStableChildReadyVisibleScrollRange();
            int scrollSiblingTop = view.getTotalScrollRange();
            int temp = (scrollSiblingTop - scrollSiblingReady) / 2 + scrollSiblingReady;
            if (scrollSibling < temp) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        boolean canDragView(DdHeaderLayout view) {
            // Else we'll use the default behaviour of seeing if it can scroll down
            if (mLastNestedScrollingChildRef != null) {
                // If we have a reference to a scrolling view, check it
                final View scrollingView = mLastNestedScrollingChildRef.get();
//                return scrollingView != null && scrollingView.isShown()
//                        && !ViewCompat.canScrollVertically(scrollingView, -1);
                return scrollingView != null && scrollingView.isShown();
            } else {
                // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
                return true;
            }
        }

        private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, DdHeaderLayout view) {
            if (getScrollState() == SCROLL_STATE_STABLE_CHILD_MIDDLE) {
                int dy = -getTopBottomOffsetForScrollingSibling() - getOffsetWhenTopState(view);
                autoScroll(coordinatorLayout, view, dy);
            }
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout,
                                     final DdHeaderLayout child, int offset) {
            if (mAnimator == null) {
                mAnimator = ViewUtils.createAnimator();
                mAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
                mAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimatorCompat animator) {
                        setHeaderTopBottomOffset(coordinatorLayout, child,
                                animator.getAnimatedIntValue());
                    }
                });
            } else {
                mAnimator.cancel();
            }

            mAnimator.setIntValues(getTopBottomOffsetForScrollingSibling(), offset);
            mAnimator.start();
        }

        @Override
        int setHeaderTopBottomOffset(CoordinatorLayout coordinatorLayout,
                                     DdHeaderLayout header, int newOffset, int minOffset, int maxOffset) {
            final int curOffset = getTopBottomOffsetForScrollingSibling();
            int consumed = 0;

            if (minOffset != 0 && curOffset >= minOffset
                    && curOffset <= maxOffset) {
                // If we have some scrolling range, and we're currently within the min and max
                // offsets, calculate a new offset
                newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);
                DdHeaderLayout ddBarLayout = (DdHeaderLayout) header;
                if (curOffset != newOffset) {
                    final int interpolatedOffset = ddBarLayout.hasChildWithInterpolator()
                            ? interpolateOffset(ddBarLayout, newOffset)
                            : newOffset;

                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

                    // Update how much dy we have consumed
                    consumed = curOffset - newOffset;
                    // Update the stored sibling offset
                    mOffsetDelta = newOffset - interpolatedOffset;
                    if (header.mDebug) {
                        Log.d("debug", "setHeaderTopBottomOffset " + interpolatedOffset + "---isBeingDragged(): " + isBeingDragged());
                    }

                    if (!offsetChanged && ddBarLayout.hasChildWithInterpolator()) {
                        // If the offset hasn't changed and we're using an interpolated scroll
                        // then we need to keep any dependent views updated. CoL will do this for
                        // us when we move, but we need to do it manually when we don't (as an
                        // interpolated scroll may finish early).
                        coordinatorLayout.dispatchDependentViewsChanged(ddBarLayout);
                    }

                    // Dispatch the updates to any listeners
                    dispatchOffsetUpdates(ddBarLayout);
                }

                //reset scroll state after dragged
                if (ddBarLayout.isReadyEnabled() && !isBeingDragged()) {
                    int scrollSibling = -getTopBottomOffsetForScrollingSibling();
                    int stableChildReadyScrollRange = header.getBottomStableChildReadyVisibleScrollRange();
                    int maxDragOffset = -getMaxDragOffset(header);
                    if (scrollSibling < stableChildReadyScrollRange) {
                        setScrollState(SCROLL_STATE_STABLE_CHILD_INVISIBLE);
                    } else if (scrollSibling == stableChildReadyScrollRange) {
                        setScrollState(SCROLL_STATE_STABLE_CHILD_READY);
                    } else if (scrollSibling > stableChildReadyScrollRange && scrollSibling < maxDragOffset) {
                        setScrollState(SCROLL_STATE_STABLE_CHILD_MIDDLE);
                    } else if (scrollSibling == maxDragOffset) {
                        setScrollState(SCROLL_STATE_STABLE_CHILD_TOP);
                    }
                }
            }

            return consumed;
        }

        private void dispatchOffsetUpdates(DdHeaderLayout layout) {
            final List<OnOffsetChangedListener> listeners = layout.mListeners;

            // Iterate backwards through the list so that most recently added listeners
            // get the first chance to decide
            for (int i = 0, z = listeners.size(); i < z; i++) {
                final OnOffsetChangedListener listener = listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(layout, getTopAndBottomOffset());
                }
            }
        }

        private int interpolateOffset(DdHeaderLayout layout, final int offset) {
//            final int absOffset = Math.abs(offset);
//
//            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
//                final View child = layout.getChildAt(i);
//                final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
//                final Interpolator interpolator = childLp.getScrollInterpolator();
//
//                if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
//                    if (interpolator != null) {
//                        int childScrollableHeight = 0;
//                        final int flags = childLp.getScrollFlags();
//                        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
//                            // We're set to scroll so add the child's height plus margin
//                            childScrollableHeight += child.getHeight() + childLp.topMargin
//                                    + childLp.bottomMargin;
//
//                            if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
//                                // For a collapsing scroll, we to take the collapsed height
//                                // into account.
//                                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
//                            }
//                        }
//
//                        if (ViewCompat.getFitsSystemWindows(child)) {
//                            childScrollableHeight -= layout.getTopInset();
//                        }
//
//                        if (childScrollableHeight > 0) {
//                            final int offsetForView = absOffset - child.getTop();
//                            final int interpolatedDiff = Math.round(childScrollableHeight *
//                                    interpolator.getInterpolation(
//                                            offsetForView / (float) childScrollableHeight));
//
//                            return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
//                        }
//                    }
//
//                    // If we get to here then the view on the offset isn't suitable for interpolated
//                    // scrolling. So break out of the loop
//                    break;
//                }
//            }

            return offset;
        }

        @Override
        public int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + mOffsetDelta;
        }

        @Override
        public Parcelable onSaveInstanceState(CoordinatorLayout parent, DdHeaderLayout ddBarLayout) {
            final Parcelable superState = super.onSaveInstanceState(parent, ddBarLayout);
            final int offset = getTopAndBottomOffset();

            // Try and find the first visible child...
            for (int i = 0, count = ddBarLayout.getChildCount(); i < count; i++) {
                View child = ddBarLayout.getChildAt(i);
                final int visBottom = child.getBottom() + offset;

                if (child.getTop() + offset <= 0 && visBottom >= 0) {
                    final DdHeaderLayout.Behavior.SavedState ss = new DdHeaderLayout.Behavior.SavedState(superState);
                    ss.firstVisibleChildIndex = i;
                    ss.firstVisibileChildAtMinimumHeight =
                            visBottom == ViewCompat.getMinimumHeight(child);
                    ss.firstVisibileChildPercentageShown = visBottom / (float) child.getHeight();
                    return ss;
                }
            }

            // Else we'll just return the super state
            return superState;
        }

        @Override
        public void onRestoreInstanceState(CoordinatorLayout parent, DdHeaderLayout ddBarLayout,
                                           Parcelable state) {
            if (state instanceof DdHeaderLayout.Behavior.SavedState) {
                final DdHeaderLayout.Behavior.SavedState ss = (DdHeaderLayout.Behavior.SavedState) state;
                super.onRestoreInstanceState(parent, ddBarLayout, ss.getSuperState());
                mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
                mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
                mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
            } else {
                super.onRestoreInstanceState(parent, ddBarLayout, state);
                mOffsetToChildIndexOnLayout = INVALID_POSITION;
            }
        }

        protected static class SavedState extends BaseSavedState {
            int firstVisibleChildIndex;
            float firstVisibileChildPercentageShown;
            boolean firstVisibileChildAtMinimumHeight;

            public SavedState(Parcel source, ClassLoader loader) {
                super(source);
                firstVisibleChildIndex = source.readInt();
                firstVisibileChildPercentageShown = source.readFloat();
                firstVisibileChildAtMinimumHeight = source.readByte() != 0;
            }

            public SavedState(Parcelable superState) {
                super(superState);
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(firstVisibleChildIndex);
                dest.writeFloat(firstVisibileChildPercentageShown);
                dest.writeByte((byte) (firstVisibileChildAtMinimumHeight ? 1 : 0));
            }

            public static final Creator<SavedState> CREATOR =
                    ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
                        @Override
                        public DdHeaderLayout.Behavior.SavedState createFromParcel(Parcel source, ClassLoader loader) {
                            return new DdHeaderLayout.Behavior.SavedState(source, loader);
                        }

                        @Override
                        public DdHeaderLayout.Behavior.SavedState[] newArray(int size) {
                            return new DdHeaderLayout.Behavior.SavedState[size];
                        }
                    });
        }
    }

    /**
     * Behavior which should be used by {@link View}s which can scroll vertically and support
     * nested scrolling to automatically scroll any {@link DdHeaderLayout} siblings.
     */
    public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {

        public ScrollingViewBehavior() {
        }

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            // We depend on any DdBarLayouts
            return dependency instanceof DdHeaderLayout;
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
            // First lay out the child as normal
            super.onLayoutChild(parent, child, layoutDirection);

            // Now offset us correctly to be in the correct position. This is important for things
            // like activity transitions which rely on accurate positioning after the first layout.
            final List<View> dependencies = parent.getDependencies(child);
            for (int i = 0, z = dependencies.size(); i < z; i++) {
                if (updateOffset(parent, child, dependencies.get(i))) {
                    // If we updated the offset, break out of the loop now
                    break;
                }
            }
            return true;
        }

        @Override
        public boolean onDependentViewChanged(CoordinatorLayout parent, View child,
                                              View dependency) {
            updateOffset(parent, child, dependency);
            return false;
        }

        private boolean updateOffset(CoordinatorLayout parent, View child, View dependency) {
            final CoordinatorLayout.Behavior behavior =
                    ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if (behavior instanceof DdHeaderLayout.Behavior) {
                // Offset the child so that it is below the app-bar (with any overlap)
                final int offset = ((DdHeaderLayout.Behavior) behavior).getTopBottomOffsetForScrollingSibling();
                setTopAndBottomOffset(dependency.getHeight() + offset
                        - getOverlapForOffset(dependency, offset));
                return true;
            }
            return false;
        }

        private int getOverlapForOffset(final View dependency, final int offset) {
            return 0;
        }


        @Override
        View findFirstDependency(List<View> views) {
            for (int i = 0, z = views.size(); i < z; i++) {
                View view = views.get(i);
                if (view instanceof DdHeaderLayout) {
                    return view;
                }
            }
            return null;
        }

        @Override
        int getScrollRange(View v) {
            if (v instanceof DdHeaderLayout) {
                return ((DdHeaderLayout) v).getTotalScrollRange();
            } else {
                return super.getScrollRange(v);
            }
        }
    }
}
