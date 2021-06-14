package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository
import com.pleon.buyt.viewmodel.StoresViewModel.SortCriterion.*
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection.ASCENDING
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection.DESCENDING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

private const val STORE_STATS_PERIOD = 14

class StoresViewModel(app: Application, private val repository: StoreRepository)
    : AndroidViewModel(app) {

    enum class SortDirection { ASCENDING, DESCENDING }

    enum class SortCriterion(@StringRes val nameRes: Int, @DrawableRes val imgRes: Int) {
        TOTAL_SPENDING(R.string.menu_text_sort_totalSpending, R.drawable.ic_price),
        PURCHASE_COUNT(R.string.menu_text_sort_purchase_count, R.drawable.ic_sigma),
        STORE_CATEGORY(R.string.menu_text_sort_category, R.drawable.ic_category),
        STORE_NAME(R.string.menu_text_sort_alphabet, R.drawable.ic_alphabet);

        fun nextCriterion() = values()[(ordinal + 1) % values().size]
    }

    data class Sort(val criterion: SortCriterion, val direction: SortDirection)

    val sort get() = sortFlow.value

    private val sortFlow = MutableStateFlow(Sort(TOTAL_SPENDING, DESCENDING))

    val stores = combine(repository.getStoreDetails(STORE_STATS_PERIOD), sortFlow) { storeDetails, sort ->
       when (sort.criterion) {
           STORE_NAME -> storeDetails.sortedBy { it.brief.store.name }
           STORE_CATEGORY -> storeDetails.sortedBy { it.brief.store.category }
           PURCHASE_COUNT -> storeDetails.sortedByDescending { it.brief.purchaseCount }
           else -> storeDetails.sortedByDescending { it.brief.totalSpending }
       }
   }.flowOn(Dispatchers.Default)

    /**
     * For [STORE_NAME], sort ascending; otherwise, sort descending.
     */
    fun toggleSort() {
        val criterion = sort.criterion.nextCriterion()
        // The direction currently is not used and stores are sorted manually in combine
        val direction = if (criterion == STORE_NAME) ASCENDING else DESCENDING
        sortFlow.value = Sort(criterion, direction)
    }

    fun flagStoreForDeletion(store: Store) = toggleStoreDeletion(store, shouldDelete = true)

    fun restoreDeletedStore(store: Store) = toggleStoreDeletion(store, shouldDelete = false)

    private fun toggleStoreDeletion(store: Store, shouldDelete: Boolean) = viewModelScope.launch {
        store.isFlaggedForDeletion = shouldDelete
        repository.updateStore(store)
    }

    fun deleteStore(store: Store) = viewModelScope.launch { repository.deleteStore(store) }
}
