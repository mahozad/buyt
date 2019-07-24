package com.pleon.buyt.ui.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.PREF_THEME
import com.pleon.buyt.ui.fragment.PREF_THEME_DARK
import com.pleon.buyt.util.LocaleUtil.setLocale
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
    }

    private fun setTheme() {
        val theme = prefs.getString(PREF_THEME, PREF_THEME_DARK)
        setTheme(when (theme) {
            PREF_THEME_DARK -> R.style.DarkTheme
            else -> R.style.LightTheme
        })
    }
}
