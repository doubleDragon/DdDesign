package saulmm.coordinatorexamples.wsl.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-17.
 */

public class CustomBehavior2 extends HeaderScrollingViewBehavior implements AppBarLayout.OnOffsetChangedListener {

    private int actionBarHeight;
    private int verticalOffset;

    public CustomBehavior2() {
    }

    public CustomBehavior2(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,
                    context.getResources().getDisplayMetrics());
        }
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
        onViewChangedBaseOnAppBar(parent, child, dependency);
        return false;
    }

    private void onViewChangedBaseOnAppBar(CoordinatorLayout parent, View child, View dependency) {
        if(isDependsOnAppbar(dependency)){
            AppBarLayout appBarLayout = (AppBarLayout) dependency;
            appBarLayout.addOnOffsetChangedListener(this);
        } else if(isDependsOnHeader(dependency)) {
            updateOffset(parent, child, dependency);
        }
    }

    private boolean isDependsOn(View dependency) {
        if(isDependsOnAppbar(dependency) || isDependsOnHeader(dependency)) {
            return true;
        }
        return false;
    }

    private boolean isDependsOnAppbar(View dependency) {
        if (dependency != null && dependency instanceof AppBarLayout) {
            if (dependency.getId() == R.id.app_bar_id) {
                return true;
            }
        }
        return false;
    }

    private boolean isDependsOnHeader(View dependency) {
        if (dependency != null && dependency instanceof NestedScrollView) {
            if (dependency.getId() == R.id.header_id) {
                return true;
            }
        }
        return false;
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

    /**
     *
     * @param parent CoordinatorLayout
     * @param child TabLayout
     * @param dependency NestedScrollView that id is header_id
     * @return
     */
    private boolean updateOffset(CoordinatorLayout parent, View child, View dependency) {
        if (isDependsOnHeader(dependency)) {
            // Offset the child so that it is below the app-bar (with any overlap)
            setTopAndBottomOffset(dependency.getBottom());
            return true;
        }
        return false;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        this.verticalOffset = verticalOffset;
    }
}
