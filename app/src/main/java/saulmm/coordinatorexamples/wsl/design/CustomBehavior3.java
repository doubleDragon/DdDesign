package saulmm.coordinatorexamples.wsl.design;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

import saulmm.coordinatorexamples.R;
import saulmm.coordinatorexamples.wsl.design.HeaderScrollingViewBehavior;

/**
 * Created by wsl on 16-8-17.
 */

public class CustomBehavior3 extends HeaderScrollingViewBehavior {

    public CustomBehavior3() {
    }

    public CustomBehavior3(Context context, AttributeSet attrs) {
        super(context, attrs);
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

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return isDependsOn(dependency);
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        updateOffset(parent, child, dependency);
        return false;
    }

    private boolean isDependsOn(View dependency) {
        if (dependency == null) {
            return false;
        }
        if (!(dependency instanceof NestedScrollView)) {
            return false;
        }
        if (dependency.getId() != R.id.header_id) {
            return false;
        }
        return true;
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
        if (isDependsOn(dependency)) {
            // Offset the child so that it is below the app-bar (with any overlap)
            setTopAndBottomOffset(dependency.getBottom());
            return true;
        }
        return false;
    }
}