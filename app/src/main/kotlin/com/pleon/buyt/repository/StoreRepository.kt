package com.pleon.buyt.repository

import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StoreRepository(private val storeDao: StoreDao) {

    fun getStoreDetails(period: Int) = storeDao.getStoreDetails(period)

    suspend fun insert(store: Store) = withContext(Dispatchers.IO) {
        storeDao.insert(store).also { store.storeId = it }
        return@withContext store
    }

    suspend fun updateStore(store: Store) = withContext(Dispatchers.IO) { storeDao.update(store) }

    suspend fun deleteStore(store: Store) = withContext(Dispatchers.IO) { storeDao.delete(store) }
}
