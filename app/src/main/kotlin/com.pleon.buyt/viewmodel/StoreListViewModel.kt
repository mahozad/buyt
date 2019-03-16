package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.database.repository.StoreRepository
import com.pleon.buyt.model.Store

class StoreListViewModel(application: Application) : AndroidViewModel(application) {

    private val storeRepository = StoreRepository(application)
    val allStores = storeRepository.all

    fun updateStore(store: Store) = storeRepository.updateStore(store)

    fun deleteStore(store: Store) = storeRepository.deleteStore(store)
}
