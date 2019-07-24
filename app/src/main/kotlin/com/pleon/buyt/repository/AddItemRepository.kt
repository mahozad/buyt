package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import com.pleon.buyt.util.SingleLiveEvent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class AddItemRepository constructor(private val itemDao: ItemDao,
                                    private val storeDao: StoreDao,
                                    private val purchaseDao: PurchaseDao) {

    val itemNameCats = itemDao.getNameCats()
    private val allStores = SingleLiveEvent<List<Store>>()

    fun addItem(item: Item) = doAsync { itemDao.insert(item) }

    fun addPurchasedItem(item: Item, store: Store, purchaseDate: Date) {
        doAsync {
            val purchase = Purchase(purchaseDate).apply { storeId = store.storeId }
            purchaseDao.insert(purchase).also { item.purchaseId = it }
            itemDao.insert(item)
        }
    }

    fun getAllStores(): LiveData<List<Store>> {
        doAsync {
            val stores = storeDao.getAllSync()
            uiThread { allStores.value = stores }
        }
        return allStores
    }
}
