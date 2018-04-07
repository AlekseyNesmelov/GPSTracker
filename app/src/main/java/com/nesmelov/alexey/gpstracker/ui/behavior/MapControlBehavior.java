package com.nesmelov.alexey.gpstracker.ui.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.nesmelov.alexey.gpstracker.R;

public class MapControlBehavior extends FloatingActionButton.Behavior {

    private static final int SPEED = 2;

    public MapControlBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        return dependency.getId() == R.id.bottom_sheet;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, FloatingActionButton child, View dependency) {
        final float ratio = (float)(parent.getHeight() - dependency.getTop()) / dependency.getHeight();
        final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        final int width = child.getWidth() + lp.getMarginEnd();
        child.setTranslationX(SPEED * ratio * width);
        return true;
    }
}
