package com.nesmelov.alexey.gpstracker.ui.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.View;

import com.nesmelov.alexey.gpstracker.R;

/**
 * Map controls adapter. The map controls should be hidden when bottom sheet is opened.
 */
public class MapControlBehavior extends FloatingActionButton.Behavior {

    private static final int SPEED = 2;

    /**
     * Constructs map controls behaviour.
     *
     * @param context context to use.
     * @param attrs attribute set to use.
     */
    public MapControlBehavior(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
        return dependency.getId() == R.id.bottom_sheet;
    }

    @Override
    public boolean onDependentViewChanged(final CoordinatorLayout parent, final FloatingActionButton child, final View dependency) {
        final float ratio = (float)(parent.getHeight() - dependency.getTop()) / dependency.getHeight();
        final CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        final int width = child.getWidth() + lp.getMarginEnd();
        child.setTranslationX(Math.max(0, SPEED * ratio * width));
        return true;
    }
}
