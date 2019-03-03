package com.pleon.buyt.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.activity.MainActivity;

import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("theme")) {
            TaskStackBuilder.create(getContext())
                    .addNextIntent(new Intent(getActivity(), MainActivity.class))
                    .addNextIntent(getActivity().getIntent())
                    .startActivities();
        }
    }
}
