package com.pleon.buyt

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.di.DaggerAppComponent
import com.pleon.buyt.repository.SubscriptionRepository
import com.pleon.buyt.util.LocaleUtil.setLocale
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject

// SKU of our product (defined in Bazaar): the premium upgrade
const val SKU_PREMIUM = "full_features"
// Does the user have the premium upgrade?
@Volatile var isPremium = false

class BuytApplication : DaggerApplication() {

    @Inject internal lateinit var iabHelper: IabHelper
    @Inject internal lateinit var subscriptionRepository: SubscriptionRepository

    override fun onCreate() {
        super.onCreate()

        // Setup stetho only for debug build mode. If android cannot recognize stetho, either make its
        // dependency in gradle a regular one (instead of debug one) or move this to DebugApplication
        // if (BuildConfig.DEBUG) Stetho.initializeWithDefaults(this)

        subscriptionRepository.getSubscription().observeForever { hasSubscription ->
            isPremium = hasSubscription
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

    // This is a very important call that stops background services and so on
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
    override fun attachBaseContext(cxt: Context) = super.attachBaseContext(setLocale(cxt))

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLocale(this)
    }
}
