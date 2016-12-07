package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wsl on 16-8-23.
 */

@CoordinatorLayout.DefaultBehavior(DdHomeLayout.Behavior.class)
public class DdHomeLayout extends LinearLayout{
    private static final int PENDING_ACTION_NONE = 0x0;
    private static final int PENDING_ACTION_EXPANDED = 0x1;
    private static final int PENDING_ACTION_COLLAPSED = 0x2;
    private static final int PENDING_ACTION_ANIMATE_ENABLED = 0x4;

    /**
     * Interface definition for a callback to be invoked when an {@link DdHomeLayout}'s vertical
     * offset changes.
     */
    public interface OnOffsetChangedListener {
        /**
         * Called when the {@link DdHomeLayout}'s layout offset has been changed. This allows
         * child views to implement custom behavior based on the offset (for instance pinning a
         * view at a certain y value).
         *
         * @param appBarLayout the {@link DdHomeLayout} which offset has changed
         * @param verticalOffset the vertical offset for the parent {@link DdHomeLayout}, in px
         */
        void onOffsetChanged(DdHomeLayout appBarLayout, int verticalOffset);
    }

    private static final int INVALID_SCROLL_RANGE = -1;

    private int mTotalScrollRange = INVALID_SCROLL_RANGE;
    private int mDownPreScrollRange = INVALID_SCROLL_RANGE;
    private int mDownScrollRange = INVALID_SCROLL_RANGE;

    boolean mHaveChildWithInterpolator;

    private float mTargetElevation;

    private int mPendingAction = PENDING_ACTION_NONE;

    private WindowInsetsCompat mLastInsets;

    private final List<OnOffsetChangedListener> mListeners;

    public DdHomeLayout(Context context) {
        this(context, null);
    }

    public DdHomeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);

        ThemeUtils.checkAppCompatTheme(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdHomeLayout);
        mTargetElevation = a.getDimensionPixelSize(R.styleable.DdHomeLayout_dd_elevation, 0);
//        setBackgroundDrawable(a.getDrawable(R.styleable.DdHomeLayout_dd_android_background));
        if (a.hasValue(R.styleable.DdHomeLayout_dd_expanded)) {
            setExpanded(a.getBoolean(R.styleable.DdHomeLayout_dd_expanded, false));
        }
        a.recycle();

        // Use the bounds view outline provider so that we cast a shadow, even without a background
        ViewUtils.setBoundsViewOutlineProvider(this);

        mListeners = new ArrayList<>();

        ViewCompat.setElevation(this, mTargetElevation);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        return setWindowInsets(insets);
                    }
                });
    }

    /**
     * Add a listener that will be called when the offset of this {@link DdHomeLayout} changes.
     *
     * @param listener The listener that will be called when the offset changes.]
     *
     * @see #removeOnOffsetChangedListener(DdHomeLayout.OnOffsetChangedListener)
     */
    public void addOnOffsetChangedListener(DdHomeLayout.OnOffsetChangedListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    /**
     * Remove the previously added {@link DdHomeLayout.OnOffsetChangedListener}.
     *
     * @param listener the listener to remove.
     */
    public void removeOnOffsetChangedListener(DdHomeLayout.OnOffsetChangedListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        invalidateScrollRanges();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        invalidateScrollRanges();

        mHaveChildWithInterpolator = false;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final DdHomeLayout.LayoutParams childLp = (DdHomeLayout.LayoutParams) child.getLayoutParams();
            final Interpolator interpolator = childLp.getScrollInterpolator();

            if (interpolator != null) {
                mHaveChildWithInterpolator = true;
                break;
            }
        }
    }

    private void invalidateScrollRanges() {
        // Invalidate the scroll ranges
        mTotalScrollRange = INVALID_SCROLL_RANGE;
        mDownPreScrollRange = INVALID_SCROLL_RANGE;
        mDownScrollRange = INVALID_SCROLL_RANGE;
    }

    @Override
    public void setOrientation(int orientation) {
        if (orientation != VERTICAL) {
            throw new IllegalArgumentException("AppBarLayout is always vertical and does"
                    + " not support horizontal orientation");
        }
        super.setOrientation(orientation);
    }

    /**
     * Sets whether this {@link DdHomeLayout} is expanded or not, animating if it has already
     * been laid out.
     *
     * <p>As with {@link DdHomeLayout}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param expanded true if the layout should be fully expanded, false if it should
     *                 be fully collapsed
     *
     * @attr ref android.support.design.R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded) {
        setExpanded(expanded, ViewCompat.isLaidOut(this));
    }

    /**
     * Sets whether this {@link DdHomeLayout} is expanded or not.
     *
     * <p>As with {@link DdHomeLayout}'s scrolling, this method relies on this layout being a
     * direct child of a {@link CoordinatorLayout}.</p>
     *
     * @param expanded true if the layout should be fully expanded, false if it should
     *                 be fully collapsed
     * @param animate Whether to animate to the new state
     *
     * @attr ref android.support.design.R.styleable#AppBarLayout_expanded
     */
    public void setExpanded(boolean expanded, boolean animate) {
        mPendingAction = (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
                | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0);
        requestLayout();
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof DdHomeLayout.LayoutParams;
    }

    @Override
    protected DdHomeLayout.LayoutParams generateDefaultLayoutParams() {
        return new DdHomeLayout.LayoutParams(DdHomeLayout.LayoutParams.MATCH_PARENT, DdHomeLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public DdHomeLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new DdHomeLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected DdHomeLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LinearLayout.LayoutParams) {
            return new DdHomeLayout.LayoutParams((LinearLayout.LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new DdHomeLayout.LayoutParams((MarginLayoutParams) p);
        }
        return new DdHomeLayout.LayoutParams(p);
    }

    private boolean hasChildWithInterpolator() {
        return mHaveChildWithInterpolator;
    }

    /**
     * Returns the scroll range of all children.
     *
     * @return the scroll range in px
     */
    public final int getTotalScrollRange() {
        if (mTotalScrollRange != INVALID_SCROLL_RANGE) {
            return mTotalScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final DdHomeLayout.LayoutParams lp = (DdHomeLayout.LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();
            final int flags = lp.mScrollFlags;

            if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childHeight + lp.topMargin + lp.bottomMargin;

                if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing scroll, we to take the collapsed height into account.
                    // We also break straight away since later views can't scroll beneath
                    // us
                    range -= ViewCompat.getMinimumHeight(child);
                    break;
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
//        return mTotalScrollRange = Math.max(0, range - getTopInset());
        return mTotalScrollRange = Math.max(0, range);
    }

    private boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    /**
     * Return the scroll range when scrolling up from a nested pre-scroll.
     */
    private int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    /**
     * Return the scroll range when scrolling down from a nested pre-scroll.
     */
    private int getDownNestedPreScrollRange() {
        if (mDownPreScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownPreScrollRange;
        }

        int range = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            final DdHomeLayout.LayoutParams lp = (DdHomeLayout.LayoutParams) child.getLayoutParams();
            final int childHeight = child.getMeasuredHeight();
            final int flags = lp.mScrollFlags;

            if ((flags & DdHomeLayout.LayoutParams.FLAG_QUICK_RETURN) == DdHomeLayout.LayoutParams.FLAG_QUICK_RETURN) {
                // First take the margin into account
                range += lp.topMargin + lp.bottomMargin;
                // The view has the quick return flag combination...
                if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
                    // If they're set to enter collapsed, use the minimum height
                    range += ViewCompat.getMinimumHeight(child);
                } else {
                    // Else use the full height
                    range += childHeight;
                }
            } else if (range > 0) {
                // If we've hit an non-quick return scrollable view, and we've already hit a
                // quick return view, return now
                break;
            }
        }
        return mDownPreScrollRange = range;
    }

    /**
     * Return the scroll range when scrolling down from a nested scroll.
     */
    private int getDownNestedScrollRange() {
        if (mDownScrollRange != INVALID_SCROLL_RANGE) {
            // If we already have a valid value, return it
            return mDownScrollRange;
        }

        int range = 0;
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            final DdHomeLayout.LayoutParams lp = (DdHomeLayout.LayoutParams) child.getLayoutParams();
            int childHeight = child.getMeasuredHeight();
            childHeight += lp.topMargin + lp.bottomMargin;

            final int flags = lp.mScrollFlags;

            if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                // We're set to scroll so add the child's height
                range += childHeight;

                if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                    // For a collapsing exit scroll, we to take the collapsed height into account.
                    // We also break the range straight away since later views can't scroll
                    // beneath us
                    range -= ViewCompat.getMinimumHeight(child) + getTopInset();
                    break;
                }
            } else {
                // As soon as a view doesn't have the scroll flag, we end the range calculation.
                // This is because views below can not scroll under a fixed view.
                break;
            }
        }
        return mDownScrollRange = Math.max(0, range);
    }

    final int getMinimumHeightForVisibleOverlappingContent() {
        final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            // If this layout has a min height, use it (doubled)
            return (minHeight * 2) + topInset;
        }

        // Otherwise, we'll use twice the min height of our last child
        final int childCount = getChildCount();
        return childCount >= 1
                ? (ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) * 2) + topInset
                : 0;
    }

    /**
     * Set the elevation value to use when this {@link DdHomeLayout} should be elevated
     * above content.
     * <p>
     * This method does not do anything itself. A typical use for this method is called from within
     * an {@link DdHomeLayout.OnOffsetChangedListener} when the offset has changed in such a way to require an
     * elevation change.
     *
     * @param elevation the elevation value to use.
     *
     * @see ViewCompat#setElevation(View, float)
     */
    public void setTargetElevation(float elevation) {
        mTargetElevation = elevation;
    }

    /**
     * Returns the elevation value to use when this {@link DdHomeLayout} should be elevated
     * above content.
     */
    public float getTargetElevation() {
        return mTargetElevation;
    }

    private int getPendingAction() {
        return mPendingAction;
    }

    private void resetPendingAction() {
        mPendingAction = PENDING_ACTION_NONE;
    }

    public int getTopInset() {
        return mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
    }

    private WindowInsetsCompat setWindowInsets(WindowInsetsCompat insets) {
        // Invalidate the total scroll range...
        mTotalScrollRange = INVALID_SCROLL_RANGE;
//        mLastInsets = insets;
//
//        // Now dispatch them to our children
//        for (int i = 0, z = getChildCount(); i < z; i++) {
//            final View child = getChildAt(i);
//            insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
//            if (insets.isConsumed()) {
//                break;
//            }
//        }

        if (mLastInsets != insets) {
            mLastInsets = insets;
            requestLayout();
        }
        return insets.consumeSystemWindowInsets();
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        /** @hide */
        @IntDef(flag=true, value={
                SCROLL_FLAG_SCROLL,
                SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
                SCROLL_FLAG_ENTER_ALWAYS,
                SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
                SCROLL_FLAG_SNAP
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {}

        /**
         * The view will be scroll in direct relation to scroll events. This flag needs to be
         * set for any of the other flags to take effect. If any sibling views
         * before this one do not have this flag, then this value has no effect.
         */
        public static final int SCROLL_FLAG_SCROLL = 0x1;

        /**
         * When exiting (scrolling off screen) the view will be scrolled until it is
         * 'collapsed'. The collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 0x2;

        /**
         * When entering (scrolling on screen) the view will scroll on any downwards
         * scroll event, regardless of whether the scrolling view is also scrolling. This
         * is commonly referred to as the 'quick return' pattern.
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 0x4;

        /**
         * An additional flag for 'enterAlways' which modifies the returning view to
         * only initially scroll back to it's collapsed height. Once the scrolling view has
         * reached the end of it's scroll range, the remainder of this view will be scrolled
         * into view. The collapsed height is defined by the view's minimum height.
         *
         * @see ViewCompat#getMinimumHeight(View)
         * @see View#setMinimumHeight(int)
         */
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 0x8;

        /**
         * Upon a scroll ending, if the view is only partially visible then it will be snapped
         * and scrolled to it's closest edge. For example, if the view only has it's bottom 25%
         * displayed, it will be scrolled off screen completely. Conversely, if it's bottom 75%
         * is visible then it will be scrolled fully into view.
         */
        public static final int SCROLL_FLAG_SNAP = 0x10;

        /**
         * Internal flags which allows quick checking features
         */
        static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;
        static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;

        int mScrollFlags = SCROLL_FLAG_SCROLL;
        Interpolator mScrollInterpolator;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.DdHomeLayout_LayoutParams);
            mScrollFlags = a.getInt(R.styleable.DdHomeLayout_LayoutParams_dd_layout_scrollFlags, 0);
            if (a.hasValue(R.styleable.DdHomeLayout_LayoutParams_dd_layout_scrollInterpolator)) {
                int resId = a.getResourceId(
                        R.styleable.DdHomeLayout_LayoutParams_dd_layout_scrollInterpolator, 0);
                mScrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(
                        c, resId);
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

//        public LayoutParams(LinearLayout.LayoutParams source) {
//            super(source);
//        }
//
//        public LayoutParams(DdHomeLayout.LayoutParams source) {
//            super(source);
//            mScrollFlags = source.mScrollFlags;
//            mScrollInterpolator = source.mScrollInterpolator;
//        }

        /**
         * Set the scrolling flags.
         *
         * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL},
         *             {@link #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS},
         *             {@link #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED} and {@link #SCROLL_FLAG_SNAP }.
         *
         * @see #getScrollFlags()
         *
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollFlags
         */
        public void setScrollFlags(@DdHomeLayout.LayoutParams.ScrollFlags int flags) {
            mScrollFlags = flags;
        }

        /**
         * Returns the scrolling flags.
         *
         * @see #setScrollFlags(int)
         *
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollFlags
         */
        @DdHomeLayout.LayoutParams.ScrollFlags
        public int getScrollFlags() {
            return mScrollFlags;
        }

        /**
         * Set the interpolator to when scrolling the view associated with this
         * {@link DdHomeLayout.LayoutParams}.
         *
         * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
         *
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollInterpolator
         * @see #getScrollInterpolator()
         */
        public void setScrollInterpolator(Interpolator interpolator) {
            mScrollInterpolator = interpolator;
        }

        /**
         * Returns the {@link Interpolator} being used for scrolling the view associated with this
         * {@link DdHomeLayout.LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
         *
         * @attr ref android.support.design.R.styleable#AppBarLayout_LayoutParams_layout_scrollInterpolator
         * @see #setScrollInterpolator(Interpolator)
         */
        public Interpolator getScrollInterpolator() {
            return mScrollInterpolator;
        }
    }

    /**
     * The default {@link DdHomeLayout.Behavior} for {@link DdHomeLayout}. Implements the necessary nested
     * scroll handling with offsetting.
     */
    public static class Behavior extends HeaderBehavior<DdHomeLayout> {
        private static final int INVALID_POSITION = -1;

        /**
         * Callback to allow control over any {@link DdHomeLayout} dragging.
         */
        public static abstract class DragCallback {
            /**
             * Allows control over whether the given {@link DdHomeLayout} can be dragged or not.
             *
             * <p>Dragging is defined as a direct touch on the AppBarLayout with movement. This
             * call does not affect any nested scrolling.</p>
             *
             * @return true if we are in a position to scroll the AppBarLayout via a drag, false
             *         if not.
             */
            public abstract boolean canDrag(@NonNull DdHomeLayout appBarLayout);
        }

        private int mOffsetDelta;

        private boolean mSkipNestedPreScroll;
        private boolean mWasFlung;

        private ValueAnimatorCompat mAnimator;

        private int mOffsetToChildIndexOnLayout = INVALID_POSITION;
        private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
        private float mOffsetToChildIndexOnLayoutPerc;

        private WeakReference<View> mLastNestedScrollingChildRef;
        private DdHomeLayout.Behavior.DragCallback mOnDragCallback;

        public Behavior() {}

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onStartNestedScroll(CoordinatorLayout parent, DdHomeLayout child,
                                           View directTargetChild, View target, int nestedScrollAxes) {
            // Return true if we're nested scrolling vertically, and we have scrollable children
            // and the scrolling view is big enough to scroll
            final boolean started = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
                    && child.hasScrollableChildren()
                    && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();

            if (started && mAnimator != null) {
                // Cancel any offset animation
                mAnimator.cancel();
            }

            // A new nested scroll has started so clear out the previous ref
            mLastNestedScrollingChildRef = null;

            return started;
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, DdHomeLayout child,
                                      View target, int dx, int dy, int[] consumed) {
            if (dy != 0 && !mSkipNestedPreScroll) {
                int min, max;
                if (dy < 0) {
                    // We're scrolling down
                    min = -child.getTotalScrollRange();
//                    max = min + child.getDownNestedPreScrollRange();
                    max = 0;
                } else {
                    // We're scrolling up
                    min = -child.getUpNestedPreScrollRange();
                    max = 0;
                }
                consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
            }
        }

        @Override
        public void onNestedScroll(CoordinatorLayout coordinatorLayout, DdHomeLayout child,
                                   View target, int dxConsumed, int dyConsumed,
                                   int dxUnconsumed, int dyUnconsumed) {
            if (dyUnconsumed < 0) {
                // If the scrolling view is scrolling down but not consuming, it's probably be at
                // the top of it's content
                scroll(coordinatorLayout, child, dyUnconsumed,
                        -child.getDownNestedScrollRange(), 0);
                // Set the expanding flag so that onNestedPreScroll doesn't handle any events
                mSkipNestedPreScroll = true;
            } else {
                // As we're no longer handling nested scrolls, reset the skip flag
                mSkipNestedPreScroll = false;
            }
        }

        @Override
        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, DdHomeLayout abl,
                                       View target) {
            if (!mWasFlung) {
                // If we haven't been flung then let's see if the current view has been set to snap
                snapToChildIfNeeded(coordinatorLayout, abl);
            }

            // Reset the flags
            mSkipNestedPreScroll = false;
            mWasFlung = false;
            // Keep a reference to the previous nested scrolling child
            mLastNestedScrollingChildRef = new WeakReference<>(target);
        }

        @Override
        public boolean onNestedFling(final CoordinatorLayout coordinatorLayout,
                                     final DdHomeLayout child, View target, float velocityX, float velocityY,
                                     boolean consumed) {
            boolean flung = false;

            if (!consumed) {
                // It has been consumed so let's fling ourselves
                flung = fling(coordinatorLayout, child, -child.getTotalScrollRange(),
                        0, -velocityY);
            } else {
                // If we're scrolling up and the child also consumed the fling. We'll fake scroll
                // upto our 'collapsed' offset
                if (velocityY < 0) {
                    // We're scrolling down
                    final int targetScroll = -child.getTotalScrollRange()
                            + child.getDownNestedPreScrollRange();
                    if (getTopBottomOffsetForScrollingSibling() < targetScroll) {
                        // If we're currently not expanded more than the target scroll, we'll
                        // animate a fling
                        animateOffsetTo(coordinatorLayout, child, targetScroll);
                        flung = true;
                    }
                } else {
                    // We're scrolling up
                    final int targetScroll = -child.getUpNestedPreScrollRange();
                    if (getTopBottomOffsetForScrollingSibling() > targetScroll) {
                        // If we're currently not expanded less than the target scroll, we'll
                        // animate a fling
                        animateOffsetTo(coordinatorLayout, child, targetScroll);
                        flung = true;
                    }
                }
            }

            mWasFlung = flung;
            return flung;
        }

        /**
         * Set a callback to control any {@link DdHomeLayout} dragging.
         *
         * @param callback the callback to use, or {@code null} to use the default behavior.
         */
        public void setDragCallback(@Nullable DdHomeLayout.Behavior.DragCallback callback) {
            mOnDragCallback = callback;
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout,
                                     final DdHomeLayout child, int offset) {
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

        private View getChildOnOffset(DdHomeLayout abl, final int offset) {
            for (int i = 0, count = abl.getChildCount(); i < count; i++) {
                View child = abl.getChildAt(i);
                if (child.getTop() <= -offset && child.getBottom() >= -offset) {
                    return child;
                }
            }
            return null;
        }

        private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, DdHomeLayout abl) {
            final int offset = getTopBottomOffsetForScrollingSibling();
            final View offsetChild = getChildOnOffset(abl, offset);
            if (offsetChild != null) {
                final DdHomeLayout.LayoutParams lp = (DdHomeLayout.LayoutParams) offsetChild.getLayoutParams();
                if ((lp.getScrollFlags() & DdHomeLayout.LayoutParams.FLAG_SNAP) == DdHomeLayout.LayoutParams.FLAG_SNAP) {
                    // We're set the snap, so animate the offset to the nearest edge
                    final int childTop = -offsetChild.getTop();
                    final int childBottom = -offsetChild.getBottom();
                    animateOffsetTo(coordinatorLayout, abl,
                            offset < (childBottom + childTop) / 2 ? childBottom : childTop);
                }
            }
        }

        @Override
        public boolean onLayoutChild(CoordinatorLayout parent, DdHomeLayout abl,
                                     int layoutDirection) {
            boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

            final int pendingAction = abl.getPendingAction();
            if (pendingAction != PENDING_ACTION_NONE) {
                final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
                if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
                    final int offset = -abl.getUpNestedPreScrollRange();
                    if (animate) {
                        animateOffsetTo(parent, abl, offset);
                    } else {
                        setHeaderTopBottomOffset(parent, abl, offset);
                    }
                } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
                    if (animate) {
                        animateOffsetTo(parent, abl, 0);
                    } else {
                        setHeaderTopBottomOffset(parent, abl, 0);
                    }
                }
            } else if (mOffsetToChildIndexOnLayout >= 0) {
                View child = abl.getChildAt(mOffsetToChildIndexOnLayout);
                int offset = -child.getBottom();
                if (mOffsetToChildIndexOnLayoutIsMinHeight) {
                    offset += ViewCompat.getMinimumHeight(child);
                } else {
                    offset += Math.round(child.getHeight() * mOffsetToChildIndexOnLayoutPerc);
                }
                setTopAndBottomOffset(offset);
            }

            // Finally reset any pending states
            abl.resetPendingAction();
            mOffsetToChildIndexOnLayout = INVALID_POSITION;

            // Make sure we update the elevation
            dispatchOffsetUpdates(abl);

            return handled;
        }

        @Override
        boolean canDragView(DdHomeLayout view) {
            if (mOnDragCallback != null) {
                // If there is a drag callback set, it's in control
                return mOnDragCallback.canDrag(view);
            }

            // Else we'll use the default behaviour of seeing if it can scroll down
            if (mLastNestedScrollingChildRef != null) {
                // If we have a reference to a scrolling view, check it
                final View scrollingView = mLastNestedScrollingChildRef.get();
                return scrollingView != null && scrollingView.isShown()
                        && !ViewCompat.canScrollVertically(scrollingView, -1);
            } else {
                // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
                return true;
            }
        }

        @Override
        int getMaxDragOffset(DdHomeLayout view) {
            return -view.getDownNestedScrollRange();
        }

        @Override
        int getScrollRangeForDragFling(DdHomeLayout view) {
            return view.getTotalScrollRange();
        }

        @Override
        int setHeaderTopBottomOffset(CoordinatorLayout coordinatorLayout,
                                     DdHomeLayout header, int newOffset, int minOffset, int maxOffset) {
            final int curOffset = getTopBottomOffsetForScrollingSibling();
            int consumed = 0;

            if (minOffset != 0 && curOffset >= minOffset
                    && curOffset <= maxOffset) {
                // If we have some scrolling range, and we're currently within the min and max
                // offsets, calculate a new offset
                newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);
                DdHomeLayout appBarLayout = (DdHomeLayout) header;
                if (curOffset != newOffset) {
                    final int interpolatedOffset = appBarLayout.hasChildWithInterpolator()
                            ? interpolateOffset(appBarLayout, newOffset)
                            : newOffset;
                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

                    // Update how much dy we have consumed
                    consumed = curOffset - newOffset;
                    // Update the stored sibling offset
                    mOffsetDelta = newOffset - interpolatedOffset;

                    if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                        // If the offset hasn't changed and we're using an interpolated scroll
                        // then we need to keep any dependent views updated. CoL will do this for
                        // us when we move, but we need to do it manually when we don't (as an
                        // interpolated scroll may finish early).
                        coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                    }

                    // Dispatch the updates to any listeners
                    dispatchOffsetUpdates(appBarLayout);
                }
            }

            return consumed;
        }

        private void dispatchOffsetUpdates(DdHomeLayout layout) {
            final List<OnOffsetChangedListener> listeners = layout.mListeners;

            // Iterate backwards through the list so that most recently added listeners
            // get the first chance to decide
            for (int i = 0, z = listeners.size(); i < z; i++) {
                final DdHomeLayout.OnOffsetChangedListener listener = listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(layout, getTopAndBottomOffset());
                }
            }
        }

        private int interpolateOffset(DdHomeLayout layout, final int offset) {
            final int absOffset = Math.abs(offset);

            for (int i = 0, z = layout.getChildCount(); i < z; i++) {
                final View child = layout.getChildAt(i);
                final DdHomeLayout.LayoutParams childLp = (DdHomeLayout.LayoutParams) child.getLayoutParams();
                final Interpolator interpolator = childLp.getScrollInterpolator();

                if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
                    if (interpolator != null) {
                        int childScrollableHeight = 0;
                        final int flags = childLp.getScrollFlags();
                        if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
                            // We're set to scroll so add the child's height plus margin
                            childScrollableHeight += child.getHeight() + childLp.topMargin
                                    + childLp.bottomMargin;

                            if ((flags & DdHomeLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                                // For a collapsing scroll, we to take the collapsed height
                                // into account.
                                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
                            }
                        }

                        if (ViewCompat.getFitsSystemWindows(child)) {
                            childScrollableHeight -= layout.getTopInset();
                        }

                        if (childScrollableHeight > 0) {
                            final int offsetForView = absOffset - child.getTop();
                            final int interpolatedDiff = Math.round(childScrollableHeight *
                                    interpolator.getInterpolation(
                                            offsetForView / (float) childScrollableHeight));

                            return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
                        }
                    }

                    // If we get to here then the view on the offset isn't suitable for interpolated
                    // scrolling. So break out of the loop
                    break;
                }
            }

            return offset;
        }

        @Override
        public int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + mOffsetDelta;
        }

        @Override
        public Parcelable onSaveInstanceState(CoordinatorLayout parent, DdHomeLayout appBarLayout) {
            final Parcelable superState = super.onSaveInstanceState(parent, appBarLayout);
            final int offset = getTopAndBottomOffset();

            // Try and find the first visible child...
            for (int i = 0, count = appBarLayout.getChildCount(); i < count; i++) {
                View child = appBarLayout.getChildAt(i);
                final int visBottom = child.getBottom() + offset;

                if (child.getTop() + offset <= 0 && visBottom >= 0) {
                    final DdHomeLayout.Behavior.SavedState ss = new DdHomeLayout.Behavior.SavedState(superState);
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
        public void onRestoreInstanceState(CoordinatorLayout parent, DdHomeLayout appBarLayout,
                                           Parcelable state) {
            if (state instanceof DdHomeLayout.Behavior.SavedState) {
                final DdHomeLayout.Behavior.SavedState ss = (DdHomeLayout.Behavior.SavedState) state;
                super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
                mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
                mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
                mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
            } else {
                super.onRestoreInstanceState(parent, appBarLayout, state);
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
                        public DdHomeLayout.Behavior.SavedState createFromParcel(Parcel source, ClassLoader loader) {
                            return new DdHomeLayout.Behavior.SavedState(source, loader);
                        }

                        @Override
                        public DdHomeLayout.Behavior.SavedState[] newArray(int size) {
                            return new DdHomeLayout.Behavior.SavedState[size];
                        }
                    });
        }
    }

    /**
     * Behavior which should be used by {@link View}s which can scroll vertically and support
     * nested scrolling to automatically scroll any {@link DdHomeLayout} siblings.
     */
    public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {
        private int mOverlayTop;

        public ScrollingViewBehavior() {}

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);

            TypedArray a = context.obtainStyledAttributes(attrs,
                    android.support.design.R.styleable.ScrollingViewBehavior_Params);
            mOverlayTop = a.getDimensionPixelSize(
                    android.support.design.R.styleable.ScrollingViewBehavior_Params_behavior_overlapTop, 0);
            a.recycle();
        }

        @Override
        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            // We depend on any AppBarLayouts
            return dependency instanceof DdHomeLayout;
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
            if (behavior instanceof DdHomeLayout.Behavior) {
                // Offset the child so that it is below the app-bar (with any overlap)
                final int offset = ((DdHomeLayout.Behavior) behavior).getTopBottomOffsetForScrollingSibling();
                setTopAndBottomOffset(dependency.getHeight() + offset
                        - getOverlapForOffset(dependency, offset));
                return true;
            }
            return false;
        }

        private int getOverlapForOffset(final View dependency, final int offset) {
            if (mOverlayTop != 0 && dependency instanceof DdHomeLayout) {
                final DdHomeLayout abl = (DdHomeLayout) dependency;
                final int totalScrollRange = abl.getTotalScrollRange();
                final int preScrollDown = abl.getDownNestedPreScrollRange();

                if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
                    // If we're in a pre-scroll down. Don't use the offset at all.
                    return 0;
                } else {
                    final int availScrollRange = totalScrollRange - preScrollDown;
                    if (availScrollRange != 0) {
                        // Else we'll use a interpolated ratio of the overlap, depending on offset
                        final float percScrolled = offset / (float) availScrollRange;
                        return MathUtils.constrain(
                                Math.round((1f + percScrolled) * mOverlayTop), 0, mOverlayTop);
                    }
                }
            }
            return mOverlayTop;
        }

        /**
         * Set the distance that this view should overlap any {@link DdHomeLayout}.
         *
         * @param overlayTop the distance in px
         */
        public void setOverlayTop(int overlayTop) {
            mOverlayTop = overlayTop;
        }

        /**
         * Returns the distance that this view should overlap any {@link DdHomeLayout}.
         */
        public int getOverlayTop() {
            return mOverlayTop;
        }

        @Override
        View findFirstDependency(List<View> views) {
            for (int i = 0, z = views.size(); i < z; i++) {
                View view = views.get(i);
                if (view instanceof DdHomeLayout) {
                    return view;
                }
            }
            return null;
        }

        @Override
        int getScrollRange(View v) {
            if (v instanceof DdHomeLayout) {
                return ((DdHomeLayout) v).getTotalScrollRange();
            } else {
                return super.getScrollRange(v);
            }
        }
    }
}
