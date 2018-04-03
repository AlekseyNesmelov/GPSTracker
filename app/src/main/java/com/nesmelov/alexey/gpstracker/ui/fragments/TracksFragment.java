package com.nesmelov.alexey.gpstracker.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nesmelov.alexey.gpstracker.R;

/**
 * Represents my tracks fragment.
 */
public class TracksFragment extends Fragment {

    /**
     * Returns an instance of the track fragment.
     *
     * @return an instance of the track fragment.
     */
    public static TracksFragment getInstance() {
        return new TracksFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_tracks, container, false);
    }
}
