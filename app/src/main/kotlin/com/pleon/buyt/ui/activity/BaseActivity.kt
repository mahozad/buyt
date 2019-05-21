package com.pleon.buyt.ui.activity

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.pleon.buyt.R
import com.pleon.buyt.setLocale
import com.pleon.buyt.ui.fragment.PREF_THEME
import com.pleon.buyt.ui.fragment.PREF_THEME_DEF
import dagger.android.support.DaggerAppCompatActivity

@Suppress("unused")
val <T : LifecycleOwner> T.TAG: String get() = javaClass.simpleName

/**
 * Extends from DaggerAppCompatActivity (which itself extends from AppCompatActivity) so that the
 * activities do not have to call AndroidInjector.inject(this) for dagger dependency injection.
 */
abstract class BaseActivity : DaggerAppCompatActivity() {

    abstract fun layout(): Int

    override fun onCreate(savedState: Bundle?) {
        setTheme() // Call before anything else
        super.onCreate(savedState)
        setLocale(this)
        setContentView(layout())
        setSupportActionBar(findViewById(R.id.bottom_bar))
    }

    private fun setTheme() {
        val prefs = getDefaultSharedPreferences(this)
        val theme = prefs.getString(PREF_THEME, PREF_THEME_DEF)
        setTheme(if (theme == PREF_THEME_DEF) R.style.DarkTheme else R.style.LightTheme)
    }
}
