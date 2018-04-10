package com.nesmelov.alexey.gpstracker.ui.binding;

import android.databinding.BindingAdapter;
import android.support.design.widget.FloatingActionButton;

public class DataBindingAdapters {

    @BindingAdapter("android:src")
    public static void setImageResource(final FloatingActionButton imageView, final int resource){
        imageView.setImageResource(resource);
    }
}
