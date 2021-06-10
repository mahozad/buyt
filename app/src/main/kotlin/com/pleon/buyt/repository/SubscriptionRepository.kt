package com.pleon.buyt.repository

import com.pleon.buyt.database.dao.SubscriptionDao
import com.pleon.buyt.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionRepository(private val subscriptionDao: SubscriptionDao) {

    suspend fun insertSubscription(subscription: Subscription) = withContext(Dispatchers.IO) {
        subscriptionDao.insertSubscription(subscription)
    }

    suspend fun getSubscriptionToken() = withContext(Dispatchers.IO) {
        subscriptionDao.getToken()
    }
}
