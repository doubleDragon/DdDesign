package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wsl on 16-8-19.
 */

public class DdSecondLayout extends ViewGroup {

    private static final String TAG = DdSecondLayout.class.getSimpleName();

    public DdSecondLayout(Context context) {
        this(context, null);
    }

    public DdSecondLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdSecondLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        int maxHeight = 0;
        int childState = 0;

        boolean setMinHeight = false;

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

            if(lp.isMinimumHeight && !setMinHeight) {
                setMinHeight = true;
                setMinimumHeight(childHeight + lp.topMargin + lp.bottomMargin);
            }
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

        boolean isMinimumHeight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.DdSecondLayout_LayoutParams);
            isMinimumHeight = a.getBoolean(R.styleable.DdSecondLayout_LayoutParams_dd_minimum_height, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}