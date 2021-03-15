package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.PurchaseDetail
import com.pleon.buyt.database.dto.Stats
import com.pleon.buyt.model.Category
import com.pleon.buyt.repository.StatsRepository
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.viewmodel.StatsViewModel.Period.NARROW

class StatsViewModel(private val app: Application, repository: StatsRepository) : AndroidViewModel(app) {

    enum class Period(var length: Int, val imageRes: Int) {
        NARROW(7, R.drawable.avd_period_wid_nar),
        MEDIUM(15, R.drawable.avd_period_nar_med),
        EXTENDED(30, R.drawable.avd_period_med_ext),
        WIDE(90, R.drawable.avd_period_ext_wid)
    }

    interface Filter {
        val criterion: String
        val imgRes: Int
    }

    // Special Case Design Pattern
    object NoFilter : Filter {
        override val criterion = "NoFilter"
        override val imgRes = R.drawable.ic_filter_off
    }

    private val triggerUpdate = MutableLiveData(true)

    val stats: LiveData<Stats> = switchMap(triggerUpdate) { repository.getStats(period.length, filter) }

    private val rawPurchaseDetails: LiveData<List<PurchaseDetail>> = switchMap(triggerUpdate) {
        repository.getPurchaseDetails(period.length, filter)
    }

    val purchaseDetails: LiveData<List<Any>> = map(rawPurchaseDetails) { details ->
        details.groupBy { formatDate(it.purchase.date) }.flatMap { listOf(it.key) + it.value }
    }

    val filterList = listOf(SelectDialogRow(app.getString(R.string.no_filter), NoFilter.imgRes)) + Category.values().map {
        SelectDialogRow(app.getString(it.nameRes), it.imageRes)
    }
    var filter: Filter = NoFilter
        private set
    var period = NARROW
        private set

    fun togglePeriod() {
        val currentIndex = Period.valueOf(period.name).ordinal
        period = Period.values()[(currentIndex + 1) % Period.values().size]
        triggerUpdate()
    }

    fun setFilter(index: Int) {
        assert(index >= 0)
        filter = getFilterByIndex(index)
        triggerUpdate()
    }

    fun triggerUpdate() = triggerUpdate.setValue(true)

    private fun getFilterByIndex(index: Int): Filter {
        val name = filterList[index].name
        for (cat in Category.values()) if (app.getString(cat.nameRes) == name) return cat
        return NoFilter
    }
}
