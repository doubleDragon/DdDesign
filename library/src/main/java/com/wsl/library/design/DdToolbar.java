package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

/**
 * Fake ToolBar, just use in DdHeaderLayout
 * Created by wsl on 16-8-30.
 */

public class DdToolbar extends LinearLayout {

    private ViewUpdateListener mViewUpdateListener;
    private boolean mOffsetEnable;

    public DdToolbar(Context context) {
        this(context, null);
    }

    public DdToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdToolbar);
        mOffsetEnable = a.getBoolean(R.styleable.DdToolbar_dd_offset_enable, true);
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (mOffsetEnable && parent instanceof DdCollapsingBarLayout) {
            if (mViewUpdateListener == null) {
                mViewUpdateListener = new ViewUpdateListener();
            }
            ((DdCollapsingBarLayout) parent).addOffsetListener(mViewUpdateListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        final ViewParent parent = getParent();
        if (mOffsetEnable && mViewUpdateListener != null && parent instanceof DdCollapsingBarLayout) {
            ((DdCollapsingBarLayout) parent).removeOffsetListener(mViewUpdateListener);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        for (int i = 0, z = getChildCount(); i < z; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.isVerticalOffset()) {
                child.offsetTopAndBottom(getHeight());
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

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected LinearLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public LinearLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends LinearLayout.LayoutParams {

        private boolean changeAlpha;
        private boolean verticalOffset;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdToolbar_LayoutParams);
            changeAlpha = a.getBoolean(R.styleable.DdToolbar_LayoutParams_dd_change_alpha, false);
            verticalOffset = a.getBoolean(R.styleable.DdToolbar_LayoutParams_dd_vertical_offset, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
            this.changeAlpha = false;
            this.verticalOffset = false;
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
            this.changeAlpha = false;
            this.verticalOffset = false;
        }

        boolean isChangeAlpha() {
            return changeAlpha;
        }

        void setChangeAlpha(boolean changeAlpha) {
            this.changeAlpha = changeAlpha;
        }

        boolean isVerticalOffset() {
            return verticalOffset;
        }

        void setVerticalOffset(boolean verticalOffset) {
            this.verticalOffset = verticalOffset;
        }
    }

    private class ViewUpdateListener implements DdCollapsingBarLayout.OffsetListener {

        @Override
        public void onOffset(int start, int end, int verticalOffset) {
            int alpha = -verticalOffset <= start ? 0xFF : 0x0;
            for (int i = 0, z = getChildCount(); i < z; i++) {
                View child = getChildAt(i);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.isChangeAlpha()) {
                    if (child instanceof DdToolBarView) {
                        DdToolBarView toolBarView = (DdToolBarView) child;
                        toolBarView.setOuterAlpha(alpha);
                    } else {
                        Drawable drawable = child.getBackground();
                        if (drawable != null) {
                            drawable.setAlpha(alpha);
                        }
                    }
                }
                if (lp.isVerticalOffset()) {
                    int topAndBottomOffset = MathUtils.constrain(verticalOffset + end, -getHeight(), 0);
                    getViewOffsetHelper(child).setTopAndBottomOffset(topAndBottomOffset);
                }
            }
        }
    }
}