package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.repository.StatsRepository
import com.pleon.buyt.ui.dialog.SelectDialogFragment.SelectDialogRow
import com.pleon.buyt.util.formatDate
import com.pleon.buyt.viewmodel.StatsViewModel.Period.NARROW
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.util.*

class StatsViewModel(private val app: Application, repository: StatsRepository) : AndroidViewModel(app) {

    enum class Period(var length: Int, val imageRes: Int) {
        NARROW(7, R.drawable.avd_period_wid_nar),
        MEDIUM(15, R.drawable.avd_period_nar_med),
        EXTENDED(30, R.drawable.avd_period_med_ext),
        WIDE(90, R.drawable.avd_period_ext_wid);

        fun nextPeriod() = values()[(ordinal + 1) % values().size]
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

    val period get() = periodFlow.value
    val filter get() = filterFlow.value
    // Update the stats when date changes (i.e. time changes from 23:59 to 00:00)
    private val dateFlow = flow {
        while (true /* OR currentCoroutineContext().isActive */) {
            emit(Date().date)
            delay(1000)
        }
    }.distinctUntilChanged()

    /**
     * StateFlow is a specialized SharedFlow in that it only retains the latest
     * value (hence the name *State*Flow). In other words, it is appropriate for
     * when we want to have the latest value or state of something in our app.
     */
    private val periodFlow = MutableStateFlow(NARROW)
    private val filterFlow = MutableStateFlow<Filter>(NoFilter)

    val stats = combine(periodFlow, filterFlow, dateFlow) { period, filter, _ ->
        repository.getStats(period.length, filter)
    }

    val purchaseDetails = combine(periodFlow, filterFlow, dateFlow) { period, filter, _ ->
        repository.getPurchaseDetails(period.length, filter)
    }.map { details ->
        details.groupBy { formatDate(it.purchase.date) }.flatMap { listOf(it.key) + it.value }
    }.flowOn(Dispatchers.Default)

    val filterList = listOf(SelectDialogRow(app.getString(R.string.no_filter), NoFilter.imgRes)) +
            Category.values().map { SelectDialogRow(app.getString(it.nameRes), it.imageRes) }

    fun togglePeriod() {
        periodFlow.value = period.nextPeriod()
    }

    fun setFilter(index: Int) {
        val name = filterList[index].name
        var newFilter: Filter = NoFilter
        for (cat in Category.values()) {
            if (name == app.getString(cat.nameRes)) {
                newFilter = cat
            }
        }
        filterFlow.value = newFilter
    }
}
