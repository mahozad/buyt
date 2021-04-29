package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.switchMap
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository
import com.pleon.buyt.viewmodel.StoresViewModel.Sort.STORE_NAME
import com.pleon.buyt.viewmodel.StoresViewModel.Sort.TOTAL_SPENDING
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection.ASCENDING
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection.DESCENDING

private const val STORE_STATS_PERIOD = 14

class StoresViewModel(app: Application, private val repository: StoreRepository)
    : AndroidViewModel(app) {

    enum class SortDirection { ASCENDING, DESCENDING }

    enum class Sort(val nameRes: Int, val imgRes: Int) {
        TOTAL_SPENDING(R.string.menu_text_sort_totalSpending, R.drawable.ic_price),
        PURCHASE_COUNT(R.string.menu_text_sort_purchase_count, R.drawable.ic_sigma),
        STORE_CATEGORY(R.string.menu_text_sort_category, R.drawable.ic_category),
        STORE_NAME(R.string.menu_text_sort_alphabet, R.drawable.ic_alphabet)
    }

    data class SortAndDeletionTrigger(
            val sort: Sort = TOTAL_SPENDING,
            // Every time a delete/restore happens increase by one
            val deletionFlag: Int = 0
    )

    private val updateTrigger = MutableLiveData(SortAndDeletionTrigger())

    val sort get() = updateTrigger.value?.sort ?: TOTAL_SPENDING

    val stores: LiveData<List<StoreDetail>> = switchMap(updateTrigger, Function {
        // For name sort ascending; for others sort descending
        val sortDirection = if (it.sort == STORE_NAME) ASCENDING else DESCENDING
        return@Function repository.getStoreDetails(it.sort, sortDirection, STORE_STATS_PERIOD)
    })

    fun toggleSort() {
        val sort = Sort.values()[(sort.ordinal + 1) % Sort.values().size]
        val deleteFlag = updateTrigger.value!!.deletionFlag
        updateTrigger.value = SortAndDeletionTrigger(sort, deleteFlag)
    }

    fun flagStoreForDeletion(store: Store) {
        store.isFlaggedForDeletion = true
        repository.updateStore(store).get() // .get() to wait for the flag to be inserted
        val deleteFlag = updateTrigger.value!!.deletionFlag + 1
        updateTrigger.value = SortAndDeletionTrigger(sort, deleteFlag)
    }

    fun restoreDeletedStore(store: Store) {
        store.isFlaggedForDeletion = false
        repository.updateStore(store).get() // .get() to wait for the flag to be inserted
        val deleteFlag = updateTrigger.value!!.deletionFlag + 1
        updateTrigger.value = SortAndDeletionTrigger(sort, deleteFlag)
    }

    fun deleteStore(store: Store) = repository.deleteStore(store)
}
