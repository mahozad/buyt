package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.repository.StatisticsRepository
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.viewmodel.StatisticsViewModel.Period.NARROW

class StatisticsViewModel(val app: Application) : AndroidViewModel(app) {

    interface Filterer {
        fun getImgRes(): Int
        fun getName(): String
    }

    // Special Case Design Pattern
    object NoFilter : Filterer {
        override fun getImgRes() = R.drawable.ic_filter
        override fun getName() = "NoFilter"
    }

    enum class Period(var length: Int, val imageRes: Int) {
        NARROW(7, R.drawable.avd_period_wid_nar),
        MEDIUM(15, R.drawable.avd_period_nar_med),
        EXTENDED(30, R.drawable.avd_period_med_ext),
        WIDE(90, R.drawable.avd_period_ext_wid)
    }

    private val repository = StatisticsRepository(app)
    var period = NARROW
        private set
    var filter: Filterer = NoFilter
    val statistics get() = repository.getStatistics(period.length, filter)
    var filters = initializeFilters()
        private set

    private fun initializeFilters(): ArrayList<SelectDialogRow> {
        val filterList = arrayListOf(SelectDialogRow(app.getString(R.string.no_filter), R.drawable.ic_filter))
        for (category in Category.values()) {
            filterList.add(SelectDialogRow(app.getString(category.nameRes), category.imageRes))
        }
        return filterList
    }

    fun togglePeriod() {
        val currentIndex = Period.valueOf(period.name).ordinal
        val index = (currentIndex + 1) % Period.values().size
        period = Period.values()[index]
    }

    fun getFilterByString(str: String): Filterer {
        for (cat in Category.values()) if (app.getString(cat.nameRes) == str) return cat
        return NoFilter
    }
}
