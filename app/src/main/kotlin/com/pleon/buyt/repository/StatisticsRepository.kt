package com.pleon.buyt.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.pleon.buyt.database.SingleLiveEvent
import com.pleon.buyt.database.dto.Statistics
import com.pleon.buyt.database.getDatabase
import com.pleon.buyt.viewmodel.StatisticsViewModel.Filterer
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StatisticsRepository(application: Application) { // TODO: make this class singleton

    private val purchaseDao = getDatabase(application).purchaseDao()
    private val statistics = SingleLiveEvent<Statistics>()

    fun getStatistics(period: Int, filter: Filterer): LiveData<Statistics> {
        doAsync {
            val stats = purchaseDao.getStatistics(period, filter)
            uiThread { statistics.value = stats }
        }
        return statistics
    }
}
