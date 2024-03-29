package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository

class CreateStoreViewModel(app: Application, private val repository: StoreRepository)
    : AndroidViewModel(app) {

    suspend fun addStore(store: Store) = repository.insert(store)
}
