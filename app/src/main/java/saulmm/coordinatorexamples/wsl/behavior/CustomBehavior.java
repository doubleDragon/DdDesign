package saulmm.coordinatorexamples.wsl.behavior;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.List;

import saulmm.coordinatorexamples.R;

/**
 * Created by wsl on 16-8-17.
 */

public class CustomBehavior extends HeaderScrollingViewBehavior implements AppBarLayout.OnOffsetChangedListener {

    private int actionBarHeight;
    private int verticalOffset;

    private int childHeight;
    private int dependencyHeight;

    public CustomBehavior() {
    }

    public CustomBehavior(Context context, AttributeSet attrs) {
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
        initIfNeeded(child, dependency);
        updateOffset(parent, child, dependency);
        return false;
    }

    private void initIfNeeded(View child, View dependency) {
        if (childHeight == 0) {
            childHeight = child.getMeasuredHeight();
        }
        if(dependencyHeight == 0) {
            dependencyHeight = dependency.getMeasuredHeight();
        }
        if (isDependsOn(dependency)) {
            AppBarLayout appBarLayout = (AppBarLayout) dependency;
            appBarLayout.addOnOffsetChangedListener(this);
        }
    }

    /**
     * @param v the first dependency view
     * @return scroll range
     */
    @Override
    int getScrollRange(View v) {
        if (isDependsOn(v)) {
            return v.getMeasuredHeight() - actionBarHeight;
        } else {
            return super.getScrollRange(v);
        }
    }

    private boolean isDependsOn(View dependency) {
        if (dependency == null) {
            return false;
        }
        if (!(dependency instanceof AppBarLayout)) {
            return false;
        }
        if (dependency.getId() != R.id.app_bar_id) {
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
            final int offset = this.verticalOffset;
            setTopAndBottomOffset(dependency.getHeight() + offset);
            return true;
        }
        return false;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        this.verticalOffset = verticalOffset;
        boolean collaped = (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange());//appbar collapsed
    }

    @Override
    public void onNestedScrollAccepted(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("test", "onNestedScrollAccepted---child: " + dumpView(child) + "---directTargetChild: " + dumpView(directTargetChild) + "---target: " + dumpView(target) + "---nestedScrollAxes: " + nestedScrollAxes);
        super.onNestedScrollAccepted(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        Log.d("test", "onStartNestedScroll---child: " + dumpView(child) + "---directTargetChild: " + dumpView(directTargetChild) + "---target: " + dumpView(target) + "---nestedScrollAxes: " + nestedScrollAxes);
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dx, int dy, int[] consumed) {
//        Log.d("test", "onStartNestedScroll---child: " + child + "---target: " + target + "---dy: " + dy + "---consumed: " + dumpArray(consumed));
//        if(dy > 0) {
//            setTopAndBottomOffset(dy);
//            consumed[1] = dy;
//        }
    }

    private boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    private int getTotalScrollRange() {
        return childHeight;
    }

    private String dumpArray(int[] temp) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(temp[0]);
        sb.append(",");
        sb.append(temp[1]);
        sb.append("]");

        return sb.toString();
    }

    private String dumpView(View view) {
        StringBuilder sb = new StringBuilder();
        sb.append(view.getClass());
        sb.append("(");
        sb.append(view.getId());
        sb.append(")");
        return sb.toString();
    }
}