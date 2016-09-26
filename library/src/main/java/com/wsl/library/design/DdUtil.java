package com.wsl.library.design;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wsl on 16-8-23.
 */

public class DdUtil {

    public static String dumpView(View view) {
        StringBuilder sb = new StringBuilder();
        sb.append(view.getClass().getSimpleName());
        sb.append(" id=").append(view.getId());
        sb.append("[");
        sb.append(view.getLeft()).append(",").append(view.getTop()).append(",").append(view.getRight()).append(",").append(view.getBottom());
        sb.append("]");
        return sb.toString();
    }

    public static String dumpEvent(MotionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append(event.getAction());
        sb.append("[").append(event.getX()).append(",").append(event.getY()).append("]");
        return sb.toString();
    }
}
