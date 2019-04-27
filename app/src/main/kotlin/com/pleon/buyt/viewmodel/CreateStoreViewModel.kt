package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository

class CreateStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StoreRepository(application)

    fun addStore(store: Store) = repository.insert(store)
}
