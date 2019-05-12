package com.pleon.buyt.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.pleon.buyt.R
import com.pleon.buyt.setLocale
import com.pleon.buyt.ui.fragment.PREF_THEME
import com.pleon.buyt.ui.fragment.PREF_THEME_DEF

val <T : LifecycleOwner> T.TAG get() = javaClass.simpleName

abstract class BaseActivity : AppCompatActivity() {

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
