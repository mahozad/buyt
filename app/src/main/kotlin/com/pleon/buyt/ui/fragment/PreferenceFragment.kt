package com.pleon.buyt.ui.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import androidx.core.app.TaskStackBuilder
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.pleon.buyt.R
import com.pleon.buyt.isPremium
import com.pleon.buyt.ui.activity.MainActivity

const val PREF_LANG = "LANG"
const val PREF_THEME = "THEME"
const val PREF_NEWBIE = "NEWBIE"
const val PREF_VIBRATE = "VIBRATE"
const val PREF_SEARCH_DIST = "DISTANCE"
const val PREF_TASK_RECREATED = "TASK_RECREATED"
const val PREF_SEARCH_DIST_DEF = "50"
const val PREF_THEME_MINIMAL= "minimal"
const val PREF_THEME_DARK = "dark"

class PreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    /**
     * Use this field wherever a context is needed to prevent exceptions.
     */
    private lateinit var activity: Activity

    override fun onCreatePreferences(savedState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this)

        preferenceScreen.findPreference<ListPreference>(PREF_THEME)!!.apply {
            isEnabled = isPremium
            setTitle(if (isPremium) R.string.pref_title_theme else R.string.pref_title_theme_disabled)
        }
        preferenceScreen.findPreference<ListPreference>(PREF_LANG)!!.apply {
            isEnabled = isPremium
            setTitle(if (isPremium) R.string.pref_title_lang else R.string.pref_title_lang_disabled)
        }
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (key == PREF_THEME || key == PREF_LANG) {
            prefs.edit().putBoolean(PREF_TASK_RECREATED, true).apply()
            recreateTask()
        }
    }

    /**
     * Recreate the back stack so the new theme or language is applied to parent activities
     * (their onCreate() method is called which in turn invokes setTheme() or setLocale() method).
     * An alternative way would be to call setTheme() in onResume() callback of the main activity.
     *
     * Note: Use the [.activity] field initialized in [onAttach()][.onAttach]
     * as context to prevent exception when reset icon is pressed multiple times in a row.
     */
    private fun recreateTask() {
        TaskStackBuilder.create(activity)
                .addNextIntent(Intent(activity, MainActivity::class.java))
                .addNextIntent(activity.intent)
                .startActivities()
    }

    override fun onAttach(cxt: Context) {
        super.onAttach(cxt)
        this.activity = cxt as Activity
    }
}
