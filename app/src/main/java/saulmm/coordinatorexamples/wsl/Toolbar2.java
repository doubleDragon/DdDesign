package saulmm.coordinatorexamples.wsl;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by wsl on 16-9-1.
 */

public class Toolbar2 extends Toolbar{

    private static final String TAG = Toolbar2.class.getSimpleName();

    public Toolbar2(Context context) {
        super(context);
    }

    public Toolbar2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Toolbar2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("test", TAG + "---onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d("test", TAG + "---onLayout");
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d("test", TAG + "---onDraw");
        super.onDraw(canvas);
    }
}
