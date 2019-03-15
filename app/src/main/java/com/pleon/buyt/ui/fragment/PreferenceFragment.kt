package com.pleon.buyt.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.pleon.buyt.R
import com.pleon.buyt.ui.activity.MainActivity

class PreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    /**
     * Use this field wherever a context is needed to prevent exceptions.
     */
    private lateinit var activity: Activity

    override fun onCreatePreferences(savedState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        preferences.registerOnSharedPreferenceChangeListener(this)
    }

    /**
     * Use the [.activity] field initialized in [onAttach()][.onAttach]
     * as context to prevent exception when reset icon is pressed multiple times in a row.
     *
     * @param sharedPreferences
     * @param key
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == "theme") {
            sharedPreferences.edit().putBoolean("themeChanged", true).apply()
            // Recreate the back stack so the new theme is applied to parent activities
            // (their onCreate() method is called which in turn invokes setTheme() method)
            TaskStackBuilder.create(activity)
                    .addNextIntent(Intent(activity, MainActivity::class.java))
                    .addNextIntent(activity.intent)
                    .startActivities()
            // An alternative way would be to call setTheme() in onResume() callback of the main activity
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.activity = context as Activity
    }
}
