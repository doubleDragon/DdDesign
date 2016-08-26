package saulmm.coordinatorexamples.wsl;

import android.view.View;

/**
 * Created by wsl on 16-8-23.
 */

public class DdUtil {

    public static String dumpView(View view) {
        StringBuilder sb = new StringBuilder();
        sb.append(view.getClass().getSimpleName());
        sb.append("[");
        sb.append(view.getLeft()).append(",").append(view.getTop()).append(",").append(view.getRight()).append(",").append(view.getBottom());
        sb.append("]");
        return sb.toString();
    }
}
