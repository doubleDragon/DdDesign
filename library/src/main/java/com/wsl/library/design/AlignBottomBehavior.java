package com.wsl.library.design;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

/**
 * Align bottom dependency view
 * Created by wsl on 16-11-20.
 */

public class AlignBottomBehavior extends HeaderScrollingViewBehavior {

    private int targetId;

    public AlignBottomBehavior() {
        super();
    }

    public AlignBottomBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DdDependency);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int attr = a.getIndex(i);
            if(a.getIndex(i) == R.styleable.DdDependency_target){
                targetId = a.getResourceId(attr, -1);
            }
        }
        a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return isDependsOn(dependency);
    }

    @Override
    View findFirstDependency(List<View> views) {
        for (int i = 0, z = views.size(); i < z; i++) {
            View view = views.get(i);
            if (isDependsOn(view)) {
                return view;
            }
        }
        return null;
    }

    private boolean isDependsOn(View dependency) {
        return dependency.getId() == targetId;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        updateOffset(parent, child, dependency);
        return super.onDependentViewChanged(parent, child, dependency);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        // First lay out the child as normal
        super.onLayoutChild(parent, child, layoutDirection);

        // Now offset us correctly to be in the correct position. This is important for things
        // like activity transitions which rely on accurate positioning after the first layout.
        final List<View> dependencies = parent.getDependencies(child);
        for (int i = 0, z = dependencies.size(); i < z; i++) {
            if (updateOffset(parent, child, dependencies.get(i))) {
                // If we updated the offset, break out of the loop now
                break;
            }
        }
        return true;
    }

    private boolean updateOffset(CoordinatorLayout parent, View child, View dependency) {
        final CoordinatorLayout.Behavior behavior =
                ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
        if(behavior instanceof DdHeaderLayout.ScrollingViewBehavior) {
            int offset = (int) (dependency.getY() + dependency.getHeight());
            Log.d("test", "updateOffset offset: " + offset);
            setTopAndBottomOffset(offset);
            return true;
        }
        return false;
    }
}