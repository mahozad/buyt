package com.pleon.buyt

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.facebook.stetho.Stetho
import com.pleon.buyt.di.*
import com.pleon.buyt.util.LocaleUtil.setLocale
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class DebugApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        isPremium = true
        Stetho.initializeWithDefaults(this)
        startKoin {
            modules(listOf(uiModule, appModule, serviceModule,
                    databaseModule, viewModelModule, repositoryModule))
            androidLogger() // Use koin android logger
            androidContext(this@DebugApplication)
        }
    }


    // FIXME: duplicate methods. The same as the ones in BuytApplication class


    /**
     * This is for android N and higher.
     *
     * To let android resource framework to fetch and display appropriate string resources based on
     * user’s language preference, we need to override the base Context of the application
     * to have default locale configuration.
     */
    override fun attachBaseContext(cxt: Context) = super.attachBaseContext(setLocale(cxt))

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLocale(this)
    }
}
