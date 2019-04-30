package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.switchMap
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository
import com.pleon.buyt.viewmodel.StoresViewModel.Sort.TOTAL_SPENDING

class StoresViewModel(application: Application) : AndroidViewModel(application) {

    enum class Sort(val nameRes: Int, val imgRes: Int) {
        TOTAL_SPENDING(R.string.menu_text_sort_totalSpending, R.drawable.ic_price),
        PURCHASE_COUNT(R.string.menu_text_sort_purchase_count, R.drawable.ic_sigma),
        STORE_CATEGORY(R.string.menu_text_sort_category, R.drawable.ic_category),
        STORE_NAME(R.string.menu_text_sort_alphabet, R.drawable.ic_alphabet)
    }

    private val repository = StoreRepository(application)
    private val sortLiveData = MutableLiveData<Sort>(TOTAL_SPENDING)

    val storeDetails: LiveData<List<StoreDetail>> = switchMap(sortLiveData, Function { sort ->
        return@Function repository.getStoreDetails(sort)
    })

    fun toggleSort() {
        sortLiveData.value = Sort.values()[(sortLiveData.value!!.ordinal + 1) % Sort.values().size]
    }

    fun getSort() = sortLiveData.value!!

    fun updateStores(stores: Collection<Store>) {
        repository.updateStores(stores)
        sortLiveData.value = sortLiveData.value // trigger switchMap
    }

    fun flagStoreForDeletion(store: Store) {
        store.isFlaggedForDeletion = true
        updateStores(listOf(store))
    }

    fun deleteStore(store: Store) {
        repository.deleteStore(store)
        sortLiveData.value = sortLiveData.value // trigger switchMap
    }

    fun restoreDeletedStore(store: Store) {
        store.isFlaggedForDeletion = false
        updateStores(listOf(store))
    }
}
