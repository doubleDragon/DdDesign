package saulmm.coordinatorexamples.test;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by wsl on 16-10-31.
 */

public class TestLayout extends LinearLayout{

    private WindowInsetsCompat mLastInsets;

    public TestLayout(Context context) {
        this(context, null);
    }

    public TestLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ViewCompat.setOnApplyWindowInsetsListener(this,
                new android.support.v4.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        setWindowInsets(insets);
                        return insets.consumeSystemWindowInsets();
                    }
                });

    }

    private void setWindowInsets(WindowInsetsCompat insets) {
        // Invalidate the total scroll range...
        mLastInsets = insets;

        // Now dispatch them to our children
        for (int i = 0, z = getChildCount(); i < z; i++) {
            final View child = getChildAt(i);
            insets = ViewCompat.dispatchApplyWindowInsets(child, insets);
            if (insets.isConsumed()) {
                break;
            }
        }
    }
}
