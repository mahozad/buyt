package com.pleon.buyt

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.facebook.stetho.Stetho
import com.pleon.buyt.di.*
import com.pleon.buyt.util.setLocale
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

@Suppress("unused")
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
     *
     * For an example, see the item quantity in the app.
     *
     * NOTE: overriding this method caused
     *  AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
     *  to not work correctly. The workaround is to set the configuration uiMode to undefined.
     *  See BaseActivity class and https://stackoverflow.com/q/64168632 for more info.
     */
    override fun attachBaseContext(cxt: Context) {
        val resources = cxt.resources
        val configuration = Configuration(resources.configuration)
        // Required for the auto theme (Day/Night theme) to work automatically
        configuration.uiMode = Configuration.UI_MODE_NIGHT_UNDEFINED
        val baseContext = cxt.createConfigurationContext(configuration)
        setLocale(baseContext)
        super.attachBaseContext(baseContext)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLocale(this)
    }
}
