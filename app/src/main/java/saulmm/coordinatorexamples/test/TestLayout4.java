package saulmm.coordinatorexamples.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wsl on 16-11-17.
 */

public class TestLayout4 extends ViewGroup {

    private WindowInsetsCompat mLastInsets;

    public TestLayout4(Context context) {
        this(context, null);
    }

    public TestLayout4(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestLayout4(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        return onWindowInsetChanged(insets);
                    }
                });
    }

    private WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
        if (mLastInsets != insets) {
            mLastInsets = insets;
            requestLayout();
        }
        return insets.consumeSystemWindowInsets();
    }

    private int getTopInset() {
        return mLastInsets != null ? mLastInsets.getSystemWindowInsetTop() : 0;
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

            TestLayout4.LayoutParams lp = (TestLayout4.LayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            maxWidth = Math.max(maxWidth, childWidth + lp.leftMargin + lp.rightMargin);
            maxHeight += childHeight + lp.topMargin + lp.bottomMargin;
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());

//        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
//                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                maxHeight);
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
            TestLayout4.LayoutParams lp = (TestLayout4.LayoutParams) child.getLayoutParams();
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
    protected TestLayout4.LayoutParams generateDefaultLayoutParams() {
        return new TestLayout4.LayoutParams(TestLayout4.LayoutParams.MATCH_PARENT, TestLayout4.LayoutParams.MATCH_PARENT);
    }

    @Override
    public TestLayout4.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new TestLayout4.LayoutParams(getContext(), attrs);
    }

    @Override
    protected TestLayout4.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new TestLayout4.LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof TestLayout4.LayoutParams;
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
}
