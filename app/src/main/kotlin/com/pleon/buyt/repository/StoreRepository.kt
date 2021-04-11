package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.database.dto.DailyCost
import com.pleon.buyt.model.Store
import com.pleon.buyt.util.SingleLiveEvent
import com.pleon.buyt.viewmodel.StoresViewModel.Sort
import com.pleon.buyt.viewmodel.StoresViewModel.SortDirection
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class StoreRepository(private val storeDao: StoreDao) {

    private val storeStats = SingleLiveEvent<List<DailyCost>>()
    private val createdStore = SingleLiveEvent<Store>()

    fun getStores(sort: Sort, sortDirection: SortDirection) = storeDao.getAll(sort, sortDirection)

    fun getStoreStats(store: Store, period: Int): LiveData<List<DailyCost>> {
        doAsync {
            val stats = storeDao.getStoreStats(store.storeId, period)
            uiThread { storeStats.value = stats }
        }
        return storeStats
    }

    fun insert(store: Store): LiveData<Store> {
        doAsync {
            val storeId = storeDao.insert(store)
            store.storeId = storeId
            uiThread { createdStore.value = store }
        }
        return createdStore
    }

    fun updateStore(store: Store) = doAsync { storeDao.update(store) }

    fun deleteStore(store: Store) = doAsync { storeDao.delete(store) }
}
