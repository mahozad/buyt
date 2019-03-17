package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.database.repository.StoreRepository
import com.pleon.buyt.model.Store

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StoreRepository(application)
    val allStores = repository.allStores

    fun updateStore(store: Store) = repository.updateStore(store)

    fun deleteStore(store: Store) = repository.deleteStore(store)
}
