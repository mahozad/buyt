package com.pleon.buyt

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.facebook.stetho.Stetho
import com.pleon.buyt.di.DaggerAppComponent
import com.pleon.buyt.ui.fragment.PREF_LANG
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import java.util.*
import javax.inject.Inject

fun setLocale(context: Context): Context {
    val lang = getDefaultSharedPreferences(context).getString(PREF_LANG, "auto")
    val locale = if (lang == "auto") Resources.getSystem().configuration.locale else Locale(lang)
    Locale.setDefault(locale)

    val config = Configuration(context.resources.configuration)
    config.setLocale(locale)

    val cxt = context.createConfigurationContext(config)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)

    return cxt
}

class BuytApplication : Application(), HasActivityInjector {

    @Inject lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)
        DaggerAppComponent
                .builder()
                .application(this)
                .build()
                .inject(this)
    }

    override fun activityInjector() = activityInjector

    /**
     * This is for android N and higher.
     *
     * To let android resource framework to fetch and display appropriate string resources based on
     * user’s language preference, we need to override the base Context of the application
     * to have default locale configuration.
     */
    override fun attachBaseContext(cxt: Context) {
        super.attachBaseContext(setLocale(cxt))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLocale(this)
    }
}
