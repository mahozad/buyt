package com.pleon.buyt

import android.content.Context
import android.content.res.Configuration
import com.facebook.stetho.Stetho
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.di.DaggerAppComponent
import com.pleon.buyt.util.LocaleUtil.setLocale
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

class DebugApplication : DaggerApplication() {

    @Inject internal lateinit var iabHelper: IabHelper

    override fun onCreate() {
        super.onCreate()
        isPremium = true
        Stetho.initializeWithDefaults(this)
    }

    // FIXME: duplicate methods. The same as the ones in BuytApplication class

    // TODO: This is a very important call that stops background services and so on
    fun disposeIabHelper() = iabHelper.dispose()

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }

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
