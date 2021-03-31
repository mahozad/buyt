package com.pleon.buyt.ui.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.*
import com.pleon.buyt.util.setLocale
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

@Suppress("unused")
val <T : LifecycleOwner> T.TAG: String get() = javaClass.simpleName

abstract class BaseActivity : AppCompatActivity() {

    val prefs: SharedPreferences by inject()

    abstract fun layout(): Int

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setTheme()
        setLocale(this)
        setContentView(layout())
        setSupportActionBar(bottom_bar)
        // Enable automatic (Day/Night) theme to follow the system theme;
        // should be called before setContentView();
        // default value is MODE_NIGHT_FOLLOW_SYSTEM.
        // NOTE: Be aware that overriding attachBaseContext(cxt: Context) in Application
        //  class may cause this to not work correctly (requiring the app to be restarted
        //  completely to take the new system theme). See Application for the workaround.
        //  Thanks to https://stackoverflow.com/q/64168632
        // AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    private fun setTheme() {
        val theme = prefs.getString(PREF_THEME, DEFAULT_THEME_NAME)
        setTheme(when (theme) {
            PREF_THEME_AUTO -> R.style.AutoTheme
            PREF_THEME_LIGHT -> R.style.LightTheme
            PREF_THEME_DARK -> R.style.DarkTheme
            else -> DEFAULT_THEME_STYLE_RES
        })
    }
}
