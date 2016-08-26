package saulmm.coordinatorexamples.wsl.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import saulmm.coordinatorexamples.R;
import saulmm.coordinatorexamples.wsl.DdUtil;

/**
 * Created by wsl on 16-8-25.
 */

public class DdCollapsingBarLayout extends ViewGroup {

    private static final int INVALIDE_PIN_HEIGHT = -1;

    private static final int SCRIM_ANIMATION_DURATION = 600;

    private Drawable mContentScrim;
    private Drawable mStatusBarScrim;
    private int mScrimAlpha;
    private boolean mScrimsAreShown;
    private ValueAnimatorCompat mScrimAnimator;

    private DdBarLayout.OnOffsetChangedListener mOnOffsetChangedListener;

    private int mCurrentOffset;
    private int mPinHeight = INVALIDE_PIN_HEIGHT;

    private WindowInsetsCompat mLastInsets;

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
                0);
        setContentScrim(a.getDrawable(R.styleable.DdCollapsingBarLayout_dd_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.DdCollapsingBarLayout_dd_statusBarScrim));

        a.recycle();

        setWillNotDraw(false);
        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        mLastInsets = insets;
                        requestLayout();
                        return insets.consumeSystemWindowInsets();
                    }
                });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof DdBarLayout) {
            if (mOnOffsetChangedListener == null) {
                mOnOffsetChangedListener = new DdCollapsingBarLayout.OffsetUpdateListener();
            }
            ((DdBarLayout) parent).addOnOffsetChangedListener(mOnOffsetChangedListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (mOnOffsetChangedListener != null && parent instanceof DdBarLayout) {
            ((DdBarLayout) parent).removeOnOffsetChangedListener(mOnOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        int maxHeight = 0;
        int childState = 0;

        int pinHeight = 0;
        //measure content
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            maxWidth = Math.max(maxWidth, childWidth + lp.leftMargin + lp.rightMargin);
            maxHeight = Math.max(maxHeight, childHeight + lp.topMargin + lp.bottomMargin);
            if(lp.getCollapseMode() == LayoutParams.COLLAPSE_MODE_PIN) {
                pinHeight = childHeight + lp.topMargin + lp.bottomMargin;
            }
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

        maxHeight += pinHeight;

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //get the available size of child view
        final int parentLeft = this.getPaddingLeft();
        final int parentRight = r - l - getPaddingRight();
        final int parentTop = this.getPaddingTop();
        final int parentBottom = b - t - getPaddingBottom();

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

            int top = parentTop + lp.topMargin;
            int bottom = Math.min(top + childHeight, parentBottom - lp.bottomMargin);

            child.layout(left, top, right, bottom);

            if(i == 0) {
                //set bar height value to min height
                setMinimumHeight(childHeight + lp.topMargin + lp.bottomMargin);
            }
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

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        if (mContentScrim != null && mScrimAlpha > 0) {
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
                drawable.setBounds(0, 0, getWidth(), getHeight() - getPinHeight());
                drawable.setCallback(this);
                drawable.setAlpha(mScrimAlpha);
            } else {
                mContentScrim = null;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private int getPinHeight() {
        if(mPinHeight != INVALIDE_PIN_HEIGHT) {
            return mPinHeight;
        }
        int pinHeight = 0;
        int count = getChildCount();
        for(int i=0; i<count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if(lp.getCollapseMode() == LayoutParams.COLLAPSE_MODE_PIN) {
                pinHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
                break;
            }
        }
        return mPinHeight = pinHeight;
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
//            if (contentScrim != null && mToolbar != null) {
//                ViewCompat.postInvalidateOnAnimation(mToolbar);
//            }
            mScrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(DdCollapsingBarLayout.this);
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

        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;

        public static final int COLLAPSE_MODE_OFF = 0;

        public static final int COLLAPSE_MODE_PIN = 1;

        public static final int COLLAPSE_MODE_PARALLAX = 2;

        private int collapseMode;
        float mParallaxMult = DEFAULT_PARALLAX_MULTIPLIER;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdCollapsingBarLayout_LayoutParams);
            collapseMode = a.getInt(R.styleable.DdCollapsingBarLayout_LayoutParams_collapseMode, 0);
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

    private class OffsetUpdateListener implements DdBarLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(DdBarLayout layout, int verticalOffset) {
            mCurrentOffset = verticalOffset;

            final int insetTop = mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
            final int scrollRange = layout.getTotalScrollRange();
//            Log.d("test", "onOffsetChanged parent: " + DdUtil.dumpView(DdCollapsingBarLayout.this));
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final DdCollapsingBarLayout.LayoutParams lp = (DdCollapsingBarLayout.LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                switch (lp.collapseMode) {
                    case CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN:
                        Log.d("test", "onOffsetChanged pin child: " + DdUtil.dumpView(child));
                        if (getHeight() - getPinHeight() - insetTop + verticalOffset >= child.getHeight()) {
                            offsetHelper.setTopAndBottomOffset(-verticalOffset);
                        }
                        break;
                    case CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX:
                        Log.d("test", "onOffsetChanged parallax child: " + DdUtil.dumpView(child));
                        offsetHelper.setTopAndBottomOffset(
                                Math.round(-verticalOffset * lp.mParallaxMult));
                        break;
                }
            }

            // Show or hide the scrims if needed
            if (mContentScrim != null || mStatusBarScrim != null) {
                Log.d("scrim", "height: " + getHeight() + "---verticalOffset: " + verticalOffset +
                        "---scrimTriggerOffset: " + getScrimTriggerOffset() + "---insetTop: " + insetTop);
                setScrimsShown(getHeight() - getPinHeight() + verticalOffset < getScrimTriggerOffset() + insetTop);
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
