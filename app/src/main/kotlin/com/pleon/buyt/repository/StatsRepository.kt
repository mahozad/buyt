package com.pleon.buyt.repository

import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.viewmodel.StatsViewModel.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StatsRepository(private val purchaseDao: PurchaseDao) {

    suspend fun getStats(period: Int, filter: Filter) = withContext(Dispatchers.IO) {
        purchaseDao.getStats(period, filter)
    }

    suspend fun getPurchaseDetails(period: Int, filter: Filter) = withContext(Dispatchers.IO) {
        purchaseDao.getPurchaseDetails(period, filter.criterion)
    }
}
