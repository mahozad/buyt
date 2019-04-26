package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.R
import com.pleon.buyt.database.repository.StoreRepository
import com.pleon.buyt.model.Store

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    enum class Sort(val string: String, val imgRes: Int) {
        STORE_NAME("name", R.drawable.ic_alphabet),
        STORE_CATEGORY("category", R.drawable.ic_category),
        TOTAL_SPENDING("totalSpending", R.drawable.ic_price),
        PURCHASE_COUNT("purchaseCount", R.drawable.ic_sigma)
    }

    var sort = Sort.STORE_NAME
    val storeDetails get() = repository.getStoreDetails(sort.string)

    private val repository = StoreRepository(application)

    fun updateStore(store: Store) = repository.updateStore(store)

    fun deleteStore(store: Store) = repository.deleteStore(store)
}
