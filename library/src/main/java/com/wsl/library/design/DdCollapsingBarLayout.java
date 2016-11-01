package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/**
 * Just for test DdCollapsingBarLayout
 * Created by wsl on 16-8-25.
 */

public class DdCollapsingBarLayout extends ViewGroup {

    interface OffsetListener {
        void onOffset(int start, int end, int verticalOffset);
    }

    private static final int INVALID_HEIGHT_PX = -1;
    private static final int SCRIM_ANIMATION_DURATION = 600;

    private final List<OffsetListener> mListeners = new ArrayList<>();

    private Drawable mContentScrim;
    private Drawable mStatusBarScrim;
    private int mScrimAlpha;
    private int mContentScrimOffset;
    private boolean mScrimsAreShown;
    private ValueAnimatorCompat mScrimAnimator;

    private DdBarLayout.OnOffsetChangedListener mOnOffsetChangedBarListener;
    private DdHeaderLayout.OnOffsetChangedListener mOnOffsetChangedHeaderListener;

    private int mCurrentOffset;
    private int mOffHeight = INVALID_HEIGHT_PX;

    private WindowInsetsCompat mLastInsets;
    private View mPinChild;
    private boolean mConsumeTopInsets;

    public DdCollapsingBarLayout(Context context) {
        this(context, null);
    }

    public DdCollapsingBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdCollapsingBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        ThemeUtils.checkAppCompatTheme(context);

//        R.style.Widget_Design_CollapsingToolbar
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.DdCollapsingBarLayout, defStyleAttr,
                R.style.Widget_Design_DdCollapsingBar);
        setContentScrim(a.getDrawable(R.styleable.DdCollapsingBarLayout_dd_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.DdCollapsingBarLayout_dd_statusBarScrim));
        mConsumeTopInsets = a.getBoolean(R.styleable.DdCollapsingBarLayout_dd_consume_top_insets, true);

        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        if (!mConsumeTopInsets) {
                            return insets;
                        }
                        mLastInsets = insets;
                        requestLayout();
                        return insets.consumeSystemWindowInsets();
                    }
                });
    }

    private View getPinChild() {
        if (mPinChild != null) {
            return mPinChild;
        }
        View view = null;
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.getCollapseMode() == LayoutParams.COLLAPSE_MODE_PIN) {
                view = child;
                break;
            }
        }
        return mPinChild = view;
    }

    private void invalidOffset() {
        mOffHeight = INVALID_HEIGHT_PX;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof DdHeaderLayout) {
            if (mOnOffsetChangedHeaderListener == null) {
                mOnOffsetChangedHeaderListener = new DdCollapsingBarLayout.HeaderOffsetUpdateListener();
            }
            ((DdHeaderLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedHeaderListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();

        if (mOnOffsetChangedHeaderListener != null && parent instanceof DdHeaderLayout) {
            ((DdHeaderLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedHeaderListener);
        }

        super.onDetachedFromWindow();
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
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            maxWidth = Math.max(maxWidth, childWidth + lp.leftMargin + lp.rightMargin);
            switch (lp.getCollapseMode()) {

                case LayoutParams.COLLAPSE_MODE_PIN:
                case LayoutParams.COLLAPSE_MODE_PARALLAX:
                    //view index 0 or 1
                    maxHeight = Math.max(maxHeight, childHeight + lp.topMargin + lp.bottomMargin);

                    break;
                case LayoutParams.COLLAPSE_MODE_OFF:
                    //view index 2
                    maxHeight += childHeight + lp.topMargin + lp.bottomMargin;
                    break;
            }
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

//      resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT) return wrong height
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                maxHeight);
        invalidOffset();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //get the available size of child view
        final int parentLeft = this.getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();
        final int parentTop = this.getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

        int count = getChildCount();
        int parallaxBottom = 0;
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
            int top;
            int bottom;
            switch (lp.getCollapseMode()) {
                case LayoutParams.COLLAPSE_MODE_PIN:
                    top = parentTop + lp.topMargin;
                    bottom = Math.min(top + childHeight, parentBottom - lp.bottomMargin);
                    setMinimumHeight(childHeight + lp.topMargin + lp.bottomMargin);
                    break;
                case LayoutParams.COLLAPSE_MODE_PARALLAX:
                    top = parentTop + lp.topMargin;
                    bottom = Math.min(top + childHeight, parentBottom - lp.bottomMargin);
                    parallaxBottom = bottom;
                    break;
                case LayoutParams.COLLAPSE_MODE_OFF:
                default:
                    top = parallaxBottom + lp.topMargin;
                    bottom = top + childHeight;
                    break;
            }

            child.layout(left, top, right, bottom);
        }

        // Update our child view offset helpers
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);

            if (mLastInsets != null && !ViewCompat.getFitsSystemWindows(child)) {
                final int insetTop = mLastInsets.getSystemWindowInsetTop();
                if (child.getTop() < insetTop) {
                    // If the child isn't set to fit system windows but is drawing within the inset
                    // offset it down
                    child.offsetTopAndBottom(insetTop);
                }
            }

            getViewOffsetHelper(child).onViewLayout();
        }
    }

    private static ViewOffsetHelper getViewOffsetHelper(View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(android.support.design.R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(android.support.design.R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    private int getOffHeight() {
        if (isInEditMode()) {
            return INVALID_HEIGHT_PX;
        }
        if (mOffHeight != INVALID_HEIGHT_PX) {
            return mOffHeight;
        }
        int offHeight = INVALID_HEIGHT_PX;
        int count = getChildCount();
        for (int i = count - 1; i > 0; i--) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.getCollapseMode() == LayoutParams.COLLAPSE_MODE_OFF) {
                offHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                break;
            }
        }
        return mOffHeight = offHeight;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        if (mContentScrim != null) {
//            mContentScrim.setBounds(0, 0, w, h - getOffHeight());
//        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        if (getPinChild() == null && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.setBounds(0, mContentScrimOffset, getWidth(), getHeight() - getOffHeight() + mContentScrimOffset);
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

//        // Let the collapsing text helper draw it's text
//        if (mCollapsingTitleEnabled && mDrawCollapsingTitle) {
//            mCollapsingTextHelper.draw(canvas);
//        }

        // Now draw the status bar scrim
        if (mStatusBarScrim != null && mScrimAlpha > 0) {
            final int topInset = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                mStatusBarScrim.setBounds(0, -mCurrentOffset, getWidth(),
                        topInset - mCurrentOffset);
                mStatusBarScrim.mutate().setAlpha(mScrimAlpha);
                mStatusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim first when drawing the toolbar
        View pinView = getPinChild();
        if (pinView != null && child == pinView && mContentScrim != null && mScrimAlpha > 0) {
            mContentScrim.setBounds(0, mContentScrimOffset, getWidth(), getHeight() - getOffHeight() + mContentScrimOffset);
            mContentScrim.mutate().setAlpha(mScrimAlpha);
            mContentScrim.draw(canvas);
        }

        // Carry on drawing the child...
        return super.drawChild(canvas, child, drawingTime);
    }

    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (mStatusBarScrim != drawable) {
            if (mStatusBarScrim != null) {
                mStatusBarScrim.setCallback(null);
            }

            mStatusBarScrim = drawable;
            drawable.setCallback(this);
            drawable.mutate().setAlpha(mScrimAlpha);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setContentScrim(@Nullable Drawable drawable) {
        if (mContentScrim != drawable) {
            if (mContentScrim != null) {
                mContentScrim.setCallback(null);
            }
            if (drawable != null) {
                mContentScrim = drawable.mutate();
                drawable.setBounds(0, 0, getWidth(), getHeight() - getOffHeight());
                drawable.setCallback(this);
                drawable.setAlpha(mScrimAlpha);
            } else {
                mContentScrim = null;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * The additional offset used to define when to trigger the scrim visibility change.
     */
    final int getScrimTriggerOffset() {
        return 2 * ViewCompat.getMinimumHeight(this);
    }

    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    public void setScrimsShown(boolean shown, boolean animate) {
        if (mScrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            mScrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        if (mScrimAnimator == null) {
            mScrimAnimator = ViewUtils.createAnimator();
            mScrimAnimator.setDuration(SCRIM_ANIMATION_DURATION);
            mScrimAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            mScrimAnimator.setUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimatorCompat animator) {
                    setScrimAlpha(animator.getAnimatedIntValue());
                }
            });
        } else if (mScrimAnimator.isRunning()) {
            mScrimAnimator.cancel();
        }

        mScrimAnimator.setIntValues(mScrimAlpha, targetAlpha);
        mScrimAnimator.start();
    }

    private void setScrimAlpha(int alpha) {
        if (alpha != mScrimAlpha) {
//            final Drawable contentScrim = mContentScrim;
//            View pinChild = getPinChild();
//            if (contentScrim != null && pinChild != null) {
//                ViewCompat.postInvalidateOnAnimation(pinChild);
//            }
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(DdCollapsingBarLayout.this);
        }
    }

    private void dispatchOffsetToChild(int start, int end, int verticalOffset) {
        for (OffsetListener listener : mListeners) {
            listener.onOffset(start, end, verticalOffset);
        }
    }

    public void addOffsetListener(OffsetListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeOffsetListener(OffsetListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
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

        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.7f;

        /*中间的header*/
        public static final int COLLAPSE_MODE_OFF = 0;

        /*悬浮的toolbar*/
        public static final int COLLAPSE_MODE_PIN = 1;

        /*渐变的banner*/
        public static final int COLLAPSE_MODE_PARALLAX = 2;

        private int collapseMode;
        float mParallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdCollapsingBarLayout_LayoutParams);
            collapseMode = a.getInt(R.styleable.DdCollapsingBarLayout_LayoutParams_dd_collapseMode, COLLAPSE_MODE_OFF);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public int getCollapseMode() {
            return collapseMode;
        }

        public void setCollapseMode(int collapseMode) {
            this.collapseMode = collapseMode;
        }
    }

    private class HeaderOffsetUpdateListener implements DdHeaderLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(DdHeaderLayout appBarLayout, int verticalOffset) {
            mCurrentOffset = verticalOffset;

            final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            int offHeight = getOffHeight();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.collapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        //update alpha in draw method by param mScrimAlpha
                        offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(
                                Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
//                    case LayoutParams.COLLAPSE_MODE_OFF:
//                        if (getHeight() - offHeight - insetTop + verticalOffset < getMinimumHeight()) {
//                            offsetHelper.setTopAndBottomOffset(verticalOffset);
//                        }
//                        break;
                }
            }

            // Show or hide the scrims if needed
            if (mContentScrim != null || mStatusBarScrim != null) {
                int scrimTriggerOffset = getScrimTriggerOffset();
                int delta = getHeight() - insetTop - offHeight;
                //scrim显示的临界点
                int scrimDelta = delta - scrimTriggerOffset;
//                setScrimsShown(getHeight() - offHeight + verticalOffset < scrimTriggerOffset + insetTop);
                setScrimsShown(scrimDelta < -verticalOffset);

                //parallax View完全被隐藏起来的临界点,超过临界点后DdBarLayout向上偏移的同时ContentScrim向下偏移
                int contentScrimDelta = delta - scrimTriggerOffset / 2;
                if (-verticalOffset > contentScrimDelta) {
                    mContentScrimOffset = -verticalOffset - contentScrimDelta;
                } else {
                    mContentScrimOffset = 0;
                }
                //4.4版本下drawChild不调用,这个地方判断版本并强制触发
                if (mContentScrimOffset > 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    ViewCompat.postInvalidateOnAnimation(DdCollapsingBarLayout.this);
                }

                //子View根绝offset做动画
//                if(-verticalOffset > scrimDelta && -verticalOffset <= contentScrimDelta){
//
//                }
                dispatchOffsetToChild(scrimDelta, contentScrimDelta, verticalOffset);
            }

            if (mStatusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(DdCollapsingBarLayout.this);
            }

            // Update the collapsing text's fraction
//            final int expandRange = getHeight() - ViewCompat.getMinimumHeight(
//                    CollapsingToolbarLayout.this) - insetTop;
//            mCollapsingTextHelper.setExpansionFraction(
//                    Math.abs(verticalOffset) / (float) expandRange);

//            if (Math.abs(verticalOffset) == scrollRange) {
//                // If we have some pinned children, and we're offset to only show those views,
//                // we want to be elevate
//                ViewCompat.setElevation(layout, layout.getTargetElevation());
//            } else {
//                // Otherwise, we're inline with the content
//                ViewCompat.setElevation(layout, 0f);
//            }
        }
    }
}
