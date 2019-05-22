package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.StoreRepository
import javax.inject.Inject

class CreateStoreViewModel @Inject constructor(app: Application,
                                               private val repository: StoreRepository)
    : AndroidViewModel(app) {

    fun addStore(store: Store) = repository.insert(store)
}
