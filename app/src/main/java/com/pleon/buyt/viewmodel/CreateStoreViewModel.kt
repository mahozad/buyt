package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.database.repository.StoreRepository
import com.pleon.buyt.model.Store

class CreateStoreViewModel(application: Application) : AndroidViewModel(application) {

    private val mStoreRepository = StoreRepository(application)

    fun addStore(store: Store) = mStoreRepository.insert(store)
}
