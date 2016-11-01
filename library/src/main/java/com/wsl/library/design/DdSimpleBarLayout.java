package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/**
 * DdCollapsingBarLayout simple case, just two child:
 * toolbar and custom header view group
 * Created by wsl on 16-8-25.
 */

public class DdSimpleBarLayout extends ViewGroup {

    interface OffsetListener {
        void onOffset(int start, int end, int verticalOffset);
    }

    private static final int INVALID_HEIGHT_PX = -1;

    private final List<OffsetListener> mListeners = new ArrayList<>();

    private DdHeaderLayout.OnOffsetChangedListener mOnOffsetChangedHeaderListener;

    private int mOffHeight = INVALID_HEIGHT_PX;

    public DdSimpleBarLayout(Context context) {
        this(context, null);
    }

    public DdSimpleBarLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdSimpleBarLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);
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
                mOnOffsetChangedHeaderListener = new DdSimpleBarLayout.HeaderOffsetUpdateListener();
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

        /**
         * off child index = 0, pin index = 1
         * measure pin child first then off child
         * don't forget set minimum height
         */
        int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
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
                    maxHeight = Math.max(maxHeight, childHeight + lp.topMargin + lp.bottomMargin);
                    //this is very important: maybe set minimum height in xml,
                    //anyway must set base pin child height
                    if (ViewCompat.getMinimumHeight(this) != childHeight + lp.topMargin + lp.bottomMargin) {
                        setMinimumHeight(childHeight + lp.topMargin + lp.bottomMargin);
                    }
                    break;
                case LayoutParams.COLLAPSE_MODE_OFF:
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
        int pinBottom = 0;
        for (int i = count - 1; i >= 0; i--) {
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
                    pinBottom = bottom = Math.min(top + childHeight, parentBottom - lp.bottomMargin);
                    child.layout(left, top, right, bottom);
                    break;
                case LayoutParams.COLLAPSE_MODE_OFF:
                    top = pinBottom + lp.topMargin;
                    bottom = top + childHeight;
                    child.layout(left, top, right, bottom);
                    break;
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


    /**
     * The additional offset used to define when to trigger the scrim visibility change.
     */
    final int getScrimTriggerOffset() {
        return 2 * ViewCompat.getMinimumHeight(this);
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
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdSimpleBarLayout_LayoutParams);
            collapseMode = a.getInt(R.styleable.DdSimpleBarLayout_LayoutParams_dd_collapseMode, COLLAPSE_MODE_OFF);
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

            int scrimTriggerOffset = getScrimTriggerOffset();
            int delta = getHeight() - offHeight;
            int contentScrimDelta = delta - scrimTriggerOffset / 2;
            //scrim显示的临界点
            int scrimDelta = delta - scrimTriggerOffset;
            dispatchOffsetToChild(scrimDelta, contentScrimDelta, verticalOffset);

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
