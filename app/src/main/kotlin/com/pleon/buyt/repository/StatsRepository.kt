package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.util.SingleLiveEvent
import com.pleon.buyt.viewmodel.StatsViewModel.Filter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StatsRepository constructor(private val purchaseDao: PurchaseDao) {

    private val stats = SingleLiveEvent<Stats>()

    fun getStats(period: Int, filter: Filter): LiveData<Stats> {
        doAsync {
            val statistics = purchaseDao.getStats(period, filter)
            uiThread { stats.value = statistics }
        }
        return stats
    }

    fun getPurchaseDetails(period: Int, filter: Filter): LiveData<List<PurchaseDetail>> {
        return purchaseDao.getPurchaseDetails(period, filter.criterion)
    }
}
