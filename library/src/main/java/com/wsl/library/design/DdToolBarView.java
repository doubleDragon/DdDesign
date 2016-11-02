package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * DdToolbar child view, that can change alpha
 * Created by wsl on 16-10-31.
 */

public class DdToolBarView extends View {

    private Drawable mInnerDrawable;
    private Drawable mOuterDrawable;

    private Rect mInnerRect;
    private Rect mOuterRect;

    private int mOuterAlpha;

    private int mInnerToOutPadding;

    public DdToolBarView(Context context) {
        this(context, null);
    }

    public DdToolBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdToolBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mInnerRect = new Rect();
        mOuterRect = new Rect();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdToolBarView);
        setInnerDrawable(a.getDrawable(R.styleable.DdToolBarView_dd_inner_drawable));
        setOuterDrawable(a.getDrawable(R.styleable.DdToolBarView_dd_outer_drawable));
        int defaultPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                context.getResources().getDisplayMetrics());
        mInnerToOutPadding = a.getDimensionPixelSize(R.styleable.DdToolBarView_dd_inner_to_outer_padding, defaultPadding);
        mOuterAlpha = a.getInt(R.styleable.DdToolBarView_dd_outer_alpha, 0);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resizeRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mOuterDrawable != null && mOuterRect != null && outerRectValid()) {
            mOuterDrawable.setBounds(mOuterRect);
            mOuterDrawable.mutate().setAlpha(mOuterAlpha);
            mOuterDrawable.draw(canvas);
        }

        if (mInnerDrawable != null && mInnerRect != null && innerRectValid()) {
            mInnerDrawable.setBounds(mInnerRect);
            mInnerDrawable.draw(canvas);
        }
    }

    public void setInnerDrawable(@Nullable Drawable drawable) {
        if (mInnerDrawable != drawable) {
            if (mInnerDrawable != null) {
                mInnerDrawable.setCallback(null);
            }
            if (drawable != null) {
                mInnerDrawable = drawable.mutate();
                resizeRect();
                drawable.setBounds(mInnerRect);
                drawable.setCallback(this);
            } else {
                mInnerDrawable = null;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setOuterDrawable(@Nullable Drawable drawable) {
        if (mOuterDrawable != drawable) {
            if (mOuterDrawable != null) {
                mOuterDrawable.setCallback(null);
            }
            if (drawable != null) {
                mOuterDrawable = drawable.mutate();
                resizeRect();
                drawable.setBounds(mOuterRect);
                drawable.setCallback(this);
                drawable.setAlpha(mOuterAlpha);
            } else {
                mOuterDrawable = null;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void resizeRect() {
        if(mInnerDrawable == null) {
            return;
        }
        int left = (getWidth() - mInnerDrawable.getIntrinsicWidth()) / 2;
        int top = (getHeight() - mInnerDrawable.getIntrinsicHeight()) / 2;
        int right = (getWidth() + mInnerDrawable.getIntrinsicWidth()) / 2;
        int bottom = (getHeight() + mInnerDrawable.getIntrinsicHeight()) / 2;
        mInnerRect.set(left, top, right, bottom);

        mOuterRect.left = left - mInnerToOutPadding;
        mOuterRect.right = right + mInnerToOutPadding;
        mOuterRect.top = top - mInnerToOutPadding;
        mOuterRect.bottom = bottom + mInnerToOutPadding;
    }

    /**
     * inner Rect的边界都不为0
     */
    private boolean innerRectValid() {
        return mInnerRect.left != 0 &&
                mInnerRect.top != 0 &&
                mInnerRect.right != 0 &&
                mInnerRect.bottom != 0;
    }

    /**
     * inner Rect的边界都不为0
     */
    private boolean outerRectValid() {
        return mOuterRect.left != 0 &&
                mOuterRect.top != 0 &&
                mOuterRect.right != 0 &&
                mOuterRect.bottom != 0;
    }

    public void setOuterAlpha(int alpha) {
        mOuterAlpha = alpha;
        ViewCompat.postInvalidateOnAnimation(this);
    }
}