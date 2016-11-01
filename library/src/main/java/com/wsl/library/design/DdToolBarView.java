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
        resizeInnerRect();
        resizeOuterRect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mInnerDrawable != null && mInnerRect != null) {
            mInnerDrawable.setBounds(mInnerRect);
            mInnerDrawable.draw(canvas);
        }

        if (mOuterDrawable != null && mOuterRect != null) {
            mOuterDrawable.setBounds(mOuterRect);
            mOuterDrawable.mutate().setAlpha(mOuterAlpha);
            mOuterDrawable.draw(canvas);
        }
    }

    public void setInnerDrawable(@Nullable Drawable drawable) {
        if (mInnerDrawable != drawable) {
            if (mInnerDrawable != null) {
                mInnerDrawable.setCallback(null);
            }
            if (drawable != null) {
                mInnerDrawable = drawable.mutate();
                resizeInnerRect();
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
                resizeOuterRect();
                drawable.setBounds(mOuterRect);
                drawable.setCallback(this);
                drawable.setAlpha(mOuterAlpha);
            } else {
                mOuterDrawable = null;
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void resizeInnerRect() {
        int left = (getWidth() - mInnerDrawable.getIntrinsicWidth()) / 2;
        int top = (getHeight() - mInnerDrawable.getIntrinsicHeight()) / 2;
        int right = (getWidth() + mInnerDrawable.getIntrinsicWidth()) / 2;
        int bottom = (getHeight() + mInnerDrawable.getIntrinsicHeight()) / 2;
        mInnerRect.set(left, top, right, bottom);
    }

    private void resizeOuterRect() {
        if (mInnerRect != null) {
            mOuterRect.left = mInnerRect.left - mInnerToOutPadding;
            mOuterRect.right = mInnerRect.right + mInnerToOutPadding;
            mOuterRect.top = mInnerRect.top - mInnerToOutPadding;
            mOuterRect.bottom = mInnerRect.bottom + mInnerToOutPadding;
        } else {
            mOuterRect.left = 0;
            mOuterRect.top = 0;
            mOuterRect.right = getWidth();
            mOuterRect.bottom = getHeight();
        }
    }

    public void setOuterAlpha(int alpha) {
        mOuterAlpha = alpha;
        ViewCompat.postInvalidateOnAnimation(this);
    }
}