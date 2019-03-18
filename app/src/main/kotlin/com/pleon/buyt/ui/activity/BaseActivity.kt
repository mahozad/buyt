package com.pleon.buyt.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.pleon.buyt.R

abstract class BaseActivity : AppCompatActivity() {

    abstract fun layout(): Int

    override fun onCreate(savedState: Bundle?) {
        setTheme() // Call before anything else
        super.onCreate(savedState)

        setContentView(layout())
        setSupportActionBar(findViewById(R.id.bottom_bar))
    }

    private fun setTheme() {
        val preferences = getDefaultSharedPreferences(this)
        val theme = preferences.getString(KEY_PREF_THEME, DEFAULT_THEME)
        setTheme(if (theme == DEFAULT_THEME) R.style.AppTheme else R.style.LightTheme)
    }

    companion object {
        const val KEY_PREF_THEME = "theme"
        const val DEFAULT_THEME = "dark"
    }
}