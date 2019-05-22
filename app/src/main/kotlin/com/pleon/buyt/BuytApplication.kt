package com.pleon.buyt

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import android.content.res.Configuration
import com.facebook.stetho.Stetho
import com.pleon.buyt.di.DaggerAppComponent
import com.pleon.buyt.util.LocaleUtil.setLocale
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import javax.inject.Inject

class BuytApplication : Application(), HasActivityInjector, HasServiceInjector {

    @Inject internal lateinit var activityInjector: DispatchingAndroidInjector<Activity>
    @Inject internal lateinit var serviceInjector: DispatchingAndroidInjector<Service>

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

    override fun serviceInjector() = serviceInjector

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
