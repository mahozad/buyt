package com.pleon.buyt.repository

import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

// A Repository class handles data operations. It provides a clean API to the rest of the app for app data
// A Repository manages query threads and allows you to use multiple back-ends.
// In the most common example, the Repository implements the logic for deciding whether
// to fetch data from a network or use results cached in a local database.
class MainRepository(private val itemDao: ItemDao,
                     private val storeDao: StoreDao,
                     private val purchaseDao: PurchaseDao) {

    val items = itemDao.getAll()

    suspend fun updateItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.updateItem(item)
    }

    suspend fun updateItems(items: Collection<Item>) = withContext(Dispatchers.IO) {
        itemDao.updateAll(items)
    }

    suspend fun deleteItem(item: Item) = withContext(Dispatchers.IO) { itemDao.delete(item) }

    suspend fun buy(
        items: Collection<Item>,
        store: Store,
        purchaseDate: Date
    ) = withContext(Dispatchers.IO) {
        val purchase = Purchase(purchaseDate).apply { storeId = store.storeId }
        val purchaseId = purchaseDao.insert(purchase)
        for (item in items) item.purchaseId = purchaseId.also { item.isBought = true }
        itemDao.updateAll(items)
    }

    suspend fun findNearStores(cor: Coordinates, searchDist: Double) = withContext(Dispatchers.IO) {
        storeDao.getNearStores(cor.sinLat, cor.cosLat, cor.sinLng, cor.cosLng, searchDist)
    }

    suspend fun getAllStores() = withContext(Dispatchers.IO) {
        storeDao.getAllStores()
    }

    suspend fun getPurchaseCountInPeriod(period: Int) = withContext(Dispatchers.IO) {
        purchaseDao.getPurchaseCountInPeriod(period)
    }
}
