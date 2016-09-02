package com.wsl.library.design;

import android.os.Build;
import android.view.View;

class ViewUtils {

    static final ValueAnimatorCompat.Creator DEFAULT_ANIMATOR_CREATOR
            = new ValueAnimatorCompat.Creator() {
        @Override
        public ValueAnimatorCompat createAnimator() {
            return new ValueAnimatorCompat(Build.VERSION.SDK_INT >= 12
                    ? new ValueAnimatorCompatImplHoneycombMr1()
                    : new ValueAnimatorCompatImplEclairMr1());
        }
    };

    private interface ViewUtilsImpl {
        void setBoundsViewOutlineProvider(View view);
    }

    private static class ViewUtilsImplBase implements ViewUtils.ViewUtilsImpl {
        @Override
        public void setBoundsViewOutlineProvider(View view) {
            // no-op
        }
    }

    private static class ViewUtilsImplLollipop implements ViewUtils.ViewUtilsImpl {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void setBoundsViewOutlineProvider(View view) {
            ViewUtilsLollipop.setBoundsViewOutlineProvider(view);
        }
    }

    private static final ViewUtils.ViewUtilsImpl IMPL;

    static {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 21) {
            IMPL = new ViewUtils.ViewUtilsImplLollipop();
        } else {
            IMPL = new ViewUtils.ViewUtilsImplBase();
        }
    }

    static void setBoundsViewOutlineProvider(View view) {
        IMPL.setBoundsViewOutlineProvider(view);
    }

    static ValueAnimatorCompat createAnimator() {
        return DEFAULT_ANIMATOR_CREATOR.createAnimator();
    }

}