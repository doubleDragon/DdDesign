package com.wsl.library.design;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

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

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static String dumpArray(int[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(array[0]).append(",").append(array[1]);
        sb.append("]");
        return sb.toString();
    }
}
