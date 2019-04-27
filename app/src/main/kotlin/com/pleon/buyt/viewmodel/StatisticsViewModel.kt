package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.repository.StatisticsRepository
import com.pleon.buyt.viewmodel.StatisticsViewModel.Period.NARROW

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StatisticsRepository(application)
    var period = NARROW
        private set
    var filter: Category? = null
    val statistics get() = repository.getStatistics(period.length, filter)

    enum class Period(var length: Int, val imageRes: Int) {
        NARROW(7, R.drawable.avd_period_wid_nar),
        MEDIUM(15, R.drawable.avd_period_nar_med),
        EXTENDED(30, R.drawable.avd_period_med_ext),
        WIDE(90, R.drawable.avd_period_ext_wid)
    }

    fun togglePeriod() {
        val currentIndex = Period.valueOf(period.name).ordinal
        val index = (currentIndex + 1) % Period.values().size
        period = Period.values()[index]
    }
}
