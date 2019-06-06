package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.dao.SubscriptionDao
import com.pleon.buyt.model.Subscription
import com.pleon.buyt.util.SingleLiveEvent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor(private val subscriptionDao: SubscriptionDao) {

    private val subscription = SingleLiveEvent<Boolean>()

    fun insertSubscription() = doAsync { subscriptionDao.insertSubscription(Subscription()) }

    fun getSubscription(): LiveData<Boolean> {
        doAsync {
            val hasSubscription = subscriptionDao.hasSubscription()
            uiThread { subscription.value = hasSubscription }
        }
        return subscription
    }
}
