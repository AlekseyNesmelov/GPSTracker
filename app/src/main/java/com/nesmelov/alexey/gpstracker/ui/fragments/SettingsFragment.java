package com.nesmelov.alexey.gpstracker.ui.fragments;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.nesmelov.alexey.gpstracker.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment getInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_page);
    }
}
