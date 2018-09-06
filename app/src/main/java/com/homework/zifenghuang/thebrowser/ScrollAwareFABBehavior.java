package com.homework.zifenghuang.thebrowser;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class ScrollAwareFABBehavior extends FloatingActionButton.Behavior {
    // 为了被布局中的xml中引用，必须实现该构造
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        // 确保滚动方向为垂直方向
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout, final FloatingActionButton child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (dyConsumed > 0) { // 向下滑动
            animateOut(child);
        } else if (dyConsumed < 0) { // 向上滑动
            animateIn(child);
        }
    }
    // FAB移出屏幕
    private void animateOut(FloatingActionButton fab) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        int bottomMargin = layoutParams.bottomMargin;
        fab.animate().translationY(fab.getHeight() + bottomMargin).setInterpolator(new LinearInterpolator()).start();
    }
    // FAB移入屏幕
    private void animateIn(FloatingActionButton fab) {
        fab.animate().translationY(0).setInterpolator(new LinearInterpolator()).start();
    }
}