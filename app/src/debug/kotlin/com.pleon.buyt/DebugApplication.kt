/*
package com.pleon.buyt

import android.content.Context
import android.content.res.Configuration
import com.facebook.stetho.Stetho
import com.pleon.buyt.di.DaggerAppComponent
import com.pleon.buyt.util.LocaleUtil
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

class DebugApplication : DaggerApplication() {

    @Inject internal lateinit var localeUtil: LocaleUtil

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

    // FIXME: duplicate methods. The same as the ones in BuytApplication class

    */
/**
     * This is for android N and higher.
     *
     * To let android resource framework to fetch and display appropriate string resources based on
     * user’s language preference, we need to override the base Context of the application
     * to have default locale configuration.
     *//*

    override fun attachBaseContext(cxt: Context) = super.attachBaseContext(localeUtil.setLocale(this))

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeUtil.setLocale(this)
    }
}
*/
