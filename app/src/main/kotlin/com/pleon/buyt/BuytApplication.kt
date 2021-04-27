package com.pleon.buyt

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.pleon.buyt.di.*
import com.pleon.buyt.util.setLocale
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

var isPremium = false
const val SKU_PREMIUM = "full_features" // SKU of premium upgrade (defined in Bazaar)

@Suppress("unused")
class BuytApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val koinApplication = setupKoin()
        val configuration = ApplicationConfiguration()
        configuration.setupKoinLogger(koinApplication)
        configuration.setupStetho(context = this)
        configuration.setPremiumStatus()
        configuration.setupSubscription()
    }

    private fun setupKoin() = startKoin {
        modules(listOf(
                uiModule,
                appModule,
                serviceModule,
                databaseModule,
                viewModelModule,
                repositoryModule))
        androidContext(this@BuytApplication)
    }

    // To uncomment, inject the IabHelper in class
    // fun disposeIabHelper() = iabHelper.dispose()

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
