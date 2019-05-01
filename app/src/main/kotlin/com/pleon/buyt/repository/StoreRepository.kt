package com.pleon.buyt.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.pleon.buyt.database.SingleLiveEvent
import com.pleon.buyt.database.dto.StoreDetail
import com.pleon.buyt.database.getDatabase
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StoresViewModel.Sort
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StoreRepository(application: Application) {

    private val storeDao = getDatabase(application).storeDao()
    private val storeDetails = SingleLiveEvent<List<StoreDetail>>()
    private val createdStore = SingleLiveEvent<Store>()

    fun getStoreDetails(sort: Sort): LiveData<List<StoreDetail>> {
        doAsync {
            val details = storeDao.getDetails(sort)
            uiThread { storeDetails.value = details }
        }
        return storeDetails
    }

    fun insert(store: Store): LiveData<Store> {
        doAsync {
            val storeId = storeDao.insert(store)
            store.storeId = storeId
            uiThread { createdStore.value = store }
        }
        return createdStore
    }

    fun updateStores(stores: Collection<Store>) = doAsync { storeDao.updateAll(stores) }

    fun deleteStore(store: Store) = doAsync { storeDao.delete(store) }
}
