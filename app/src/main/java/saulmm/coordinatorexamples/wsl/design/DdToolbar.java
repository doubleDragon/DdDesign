package saulmm.coordinatorexamples.wsl.design;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by wsl on 16-8-30.
 */

public class DdToolbar extends LinearLayout {

    public DdToolbar(Context context) {
        this(context, null);
    }

    public DdToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DdToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}