package com.pleon.buyt

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.billing.IabResult
import com.pleon.buyt.billing.Inventory
import com.pleon.buyt.di.*
import com.pleon.buyt.repository.SubscriptionRepository
import com.pleon.buyt.util.setLocale
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.mindrot.jbcrypt.BCrypt

var isPremium = false
const val SKU_PREMIUM = "full_features" // SKU of premium upgrade (defined in Bazaar)
private const val TAG = "BuytApplication"

class BuytApplication : Application() {

    private val iabHelper by inject<IabHelper>()
    private val subscriptionRepository by inject<SubscriptionRepository>()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(listOf(uiModule, appModule, serviceModule,
                    databaseModule, viewModelModule, repositoryModule))
            androidContext(this@BuytApplication)
        }
        // For more info about saving purchase status locally see [https://stackoverflow.com/q/14231859]
        subscriptionRepository.getSubscriptionToken().observeForever { checkSubscription(it) }
    }

    private fun checkSubscription(token: String?) {
        isPremium = token != null && BCrypt.checkpw("PREMIUM", token)
        if (!isPremium) try {
            setupIabHelper()
        } catch (e: Exception) {
            Log.i(TAG, "Exception occurred in Iab setup: $e")
        }
    }

    private fun setupIabHelper() {
        iabHelper.startSetup { setupResult ->
            if (setupResult.isFailure) {
                Log.i(TAG, "Failed to setup Iab: $setupResult")
            } else {
                // Call queryInventoryAsync to find out what is already purchased
                iabHelper.queryInventoryAsync(this::onQueryResult)
            }
        }
    }

    private fun onQueryResult(result: IabResult, inventory: Inventory?) {
        if (inventory == null || result.isFailure) {
            Log.i(TAG, "Failed to query inventory: $result")
        } else {
            isPremium = inventory.hasPurchase(SKU_PREMIUM)
        }
    }

    fun disposeIabHelper() = iabHelper.dispose()

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
