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
 * Represents alarms fragment.
 */
public class AlarmFragment extends Fragment {

    /**
     * Returns alarm fragment instance.
     *
     * @return alarm fragment instance.
     */
    public static AlarmFragment getInstance() {
        return new AlarmFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_alarms, container, false);
    }
}
