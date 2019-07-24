package com.pleon.buyt

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.di.*
import com.pleon.buyt.repository.SubscriptionRepository
import com.pleon.buyt.util.LocaleUtil.setLocale
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.mindrot.jbcrypt.BCrypt

const val SKU_PREMIUM = "full_features" // SKU of premium upgrade (defined in Bazaar)
@Volatile var isPremium = false // Does the user have the premium upgrade?

class BuytApplication : Application() {

    private val iabHelper: IabHelper by inject()
    private val subscriptionRepository: SubscriptionRepository by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(listOf(
                    uiModule,
                    appModule,
                    serviceModule,
                    databaseModule,
                    viewModelModule,
                    repositoryModule)
            )
            androidContext(this@BuytApplication)
        }

        // Setup stetho only for debug build mode. If android cannot recognize stetho, either make its
        // dependency in gradle a regular one (instead of debug one) or move this to DebugApplication
        // if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)

        // For more info about saving purchase status locally see [https://stackoverflow.com/q/14231859]
        subscriptionRepository.getSubscriptionToken().observeForever { token ->
            isPremium = token != null && BCrypt.checkpw("PREMIUM", token)
            if (!isPremium) setupIabHelper()
        }
    }

    // Start in-app billing setup. This is asynchronous and the specified listener
    // will be called once setup completes.
    private fun setupIabHelper() {
        try {
            iabHelper.startSetup { setupResult ->
                if (setupResult.isFailure) {
                    // BillingErrorDialogFragment().show(supportFragmentManager, "Billing-dialog")
                } else {
                    // Call queryInventoryAsync to find out what is already purchased
                    // (if this has been called while online it always works while offline).
                    iabHelper.queryInventoryAsync { result, inventory ->
                        if (result.isFailure) {
                            Log.d("AAA", "Failed to query inventory: $result")
                        } else {
                            Log.d("AAA", "Query inventory was successful: $result")
                            isPremium = inventory.hasPurchase(SKU_PREMIUM)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // BillingErrorDialogFragment().show(supportFragmentManager, "BILLING-DIALOG")
        }
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
