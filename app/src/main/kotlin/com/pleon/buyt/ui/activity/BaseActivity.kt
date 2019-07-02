package com.pleon.buyt.ui.activity

import android.content.SharedPreferences
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.PREF_THEME
import com.pleon.buyt.ui.fragment.PREF_THEME_DEF
import com.pleon.buyt.util.LocaleUtil
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@Suppress("unused")
val <T : LifecycleOwner> T.TAG: String get() = javaClass.simpleName

/**
 * Extends from DaggerAppCompatActivity (which itself extends from AppCompatActivity) so that the
 * activities do not have to call AndroidInjector.inject(this) for dagger dependency injection.
 */
abstract class BaseActivity : DaggerAppCompatActivity() {

    @Inject internal lateinit var prefs: SharedPreferences
    @Inject internal lateinit var localeUtil: LocaleUtil

    abstract fun layout(): Int

    override fun onCreate(savedState: Bundle?) {
        super.onCreate(savedState)
        setTheme()
        localeUtil.setLocale(this)
        setContentView(layout())
        setSupportActionBar(bottom_bar)
    }

    private fun setTheme() {
        val theme = prefs.getString(PREF_THEME, PREF_THEME_DEF)
        setTheme(if (theme == PREF_THEME_DEF) R.style.DarkTheme else R.style.LightTheme)
    }
}
