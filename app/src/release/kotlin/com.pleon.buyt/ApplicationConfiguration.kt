package com.pleon.buyt

import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.pleon.buyt.billing.IabHelper
import com.pleon.buyt.billing.IabResult
import com.pleon.buyt.billing.Inventory
import com.pleon.buyt.repository.SubscriptionRepository
import org.koin.core.KoinApplication
import org.koin.java.KoinJavaComponent.inject
import org.mindrot.jbcrypt.BCrypt

private const val TAG = "BuytAppConfiguration"

class ApplicationConfiguration {

    private val iabHelper by inject(IabHelper::class.java)
    private val repository by inject(SubscriptionRepository::class.java)

    fun setPremiumStatus() {
        isPremium = false
    }

    fun setupStetho(context: Context) {
        // Do nothing for release variant
    }

    fun setupKoinLogger(koinApplication: KoinApplication) {
        // Do nothing for release variant
    }

    /**
     * For more info about saving purchase status locally see [https://stackoverflow.com/q/14231859]
     */
    fun setupSubscription() {
        repository
                .getSubscriptionToken()
                .observe(ProcessLifecycleOwner.get(), this::checkSubscription)
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
}
