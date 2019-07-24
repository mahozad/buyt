package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.dao.SubscriptionDao
import com.pleon.buyt.model.Subscription
import com.pleon.buyt.util.SingleLiveEvent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class SubscriptionRepository constructor(private val subscriptionDao: SubscriptionDao) {

    private val subscriptionToken = SingleLiveEvent<String>()

    fun insertSubscription(subscription: Subscription) = doAsync {
        subscriptionDao.insertSubscription(subscription)
    }

    fun getSubscriptionToken(): LiveData<String> {
        doAsync {
            val token = subscriptionDao.getToken()
            uiThread { subscriptionToken.value = token }
        }
        return subscriptionToken
    }
}
