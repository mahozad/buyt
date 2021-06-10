package com.pleon.buyt.repository

import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class AddItemRepository(private val itemDao: ItemDao,
                        private val storeDao: StoreDao,
                        private val purchaseDao: PurchaseDao) {

    val itemNameCats = itemDao.getItemNameCats()

    suspend fun addItem(item: Item) = withContext(Dispatchers.IO) {
        itemDao.insert(item)
    }

    suspend fun addPurchasedItem(
        item: Item,
        store: Store,
        purchaseDate: Date
    ) = withContext(Dispatchers.IO) {
        val purchase = Purchase(purchaseDate).apply { storeId = store.storeId }
        purchaseDao.insert(purchase).also { item.purchaseId = it }
        itemDao.insert(item)
    }

    suspend fun getAllStores() = withContext(Dispatchers.IO) {
        storeDao.getAllStores()
    }
}
