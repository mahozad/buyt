package com.pleon.buyt.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.pleon.buyt.R;
import com.pleon.buyt.ui.activity.MainActivity;

import androidx.annotation.NonNull;
import androidx.core.app.TaskStackBuilder;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class PreferenceFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Use this field wherever a context is needed to prevent exceptions.
     */
    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Use the {@link #activity} field initialized in {@link #onAttach(Context) onAttach()}
     * as context to prevent exception when reset icon is pressed multiple times in a row.
     *
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("theme")) {
            sharedPreferences.edit().putBoolean("themeChanged", true).apply();
            // Recreate the back stack so the new theme is applied to parent activities
            // (their onCreate() method is called which in turn invokes setTheme() method)
            TaskStackBuilder.create(activity)
                    .addNextIntent(new Intent(activity, MainActivity.class))
                    .addNextIntent(activity.getIntent())
                    .startActivities();
            // An alternative way would be to call setTheme() in onResume() callback of the main activity
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }
}
