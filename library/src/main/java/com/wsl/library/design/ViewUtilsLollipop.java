package com.wsl.library.design;

import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

class ViewUtilsLollipop {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static void setBoundsViewOutlineProvider(View view) {
        view.setOutlineProvider(ViewOutlineProvider.BOUNDS);
    }

}

