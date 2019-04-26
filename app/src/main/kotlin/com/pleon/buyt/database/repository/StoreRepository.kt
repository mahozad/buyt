package com.pleon.buyt.database.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.pleon.buyt.database.SingleLiveEvent
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.database.getDatabase
import com.pleon.buyt.model.Store
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StoreRepository(context: Context) {

    private val storeDao = getDatabase(context).storeDao()
    private val createdStore = SingleLiveEvent<Store>()
    private val storeDetails = SingleLiveEvent<List<StoreDao.StoreDetail>>()

    fun insert(store: Store/*, publishRequired: Boolean*/): LiveData<Store> {
        doAsync {
            val storeId = storeDao.insert(store)
            store.storeId = storeId
            // if (publishRequired) {
            uiThread { createdStore.value = store }
            // }
        }
        return createdStore
    }

    fun getStoreDetails(sort: String): LiveData<List<StoreDao.StoreDetail>> {
        doAsync {
            val details = storeDao.getStoreDetails(sort)
            uiThread { storeDetails.value = details }
        }
        return storeDetails
    }

    fun updateStore(store: Store) = doAsync { storeDao.update(store) }

    fun deleteStore(store: Store) = doAsync { storeDao.delete(store) }
}
