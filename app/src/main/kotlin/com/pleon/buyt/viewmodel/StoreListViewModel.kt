package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.R
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository
import com.pleon.buyt.viewmodel.StoreListViewModel.Sort.TOTAL_SPENDING

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    enum class Sort(val nameRes: Int, val imgRes: Int) {
        TOTAL_SPENDING(R.string.menu_text_sort_totalSpending, R.drawable.ic_price),
        PURCHASE_COUNT(R.string.menu_text_sort_purchase_count, R.drawable.ic_sigma),
        STORE_CATEGORY(R.string.menu_text_sort_category, R.drawable.ic_category),
        STORE_NAME(R.string.menu_text_sort_alphabet, R.drawable.ic_alphabet)
    }

    var sort = TOTAL_SPENDING
    val storeDetails get() = repository.getStoreDetails(sort)
    private val repository = StoreRepository(application)

    fun toggleSort() {
        sort = Sort.values()[(sort.ordinal + 1) % Sort.values().size]
    }

    fun updateStores(stores: Collection<Store>) = repository.updateStores(stores)

    fun deleteStore(store: Store) = repository.deleteStore(store)
}
