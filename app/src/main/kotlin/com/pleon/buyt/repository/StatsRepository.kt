package com.pleon.buyt.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.pleon.buyt.database.SingleLiveEvent
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.database.getDatabase
import com.pleon.buyt.viewmodel.StatsViewModel.Filter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StatsRepository(application: Application) {

    private val purchaseDao = getDatabase(application).purchaseDao()
    private val stats = SingleLiveEvent<Stats>()

    fun getStats(period: Int, filter: Filter): LiveData<Stats> {
        doAsync {
            val statistics = purchaseDao.getStats(period, filter)
            uiThread { stats.value = statistics }
        }
        return stats
    }
}
