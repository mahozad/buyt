package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dao.StoreDao
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import com.pleon.buyt.util.SingleLiveEvent
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

// A Repository class handles data operations. It provides a clean API to the rest of the app for app data
// A Repository manages query threads and allows you to use multiple back-ends.
// In the most common example, the Repository implements the logic for deciding whether
// to fetch data from a network or use results cached in a local database.
class MainRepository constructor(private val itemDao: ItemDao,
                                 private val storeDao: StoreDao,
                                 private val purchaseDao: PurchaseDao) {

    val items = itemDao.getAll()
    private val nearStores = SingleLiveEvent<List<Store>>()
    private val allStores = SingleLiveEvent<List<Store>>()
    private val purchaseCount = SingleLiveEvent<Int>()

    fun updateItems(items: Collection<Item>) = doAsync { itemDao.updateAll(items) }

    fun deleteItem(item: Item) = doAsync { itemDao.delete(item) }

    fun buy(items: Collection<Item>, store: Store, purchaseDate: Date) {
        doAsync {
            val purchase = Purchase(purchaseDate).apply { storeId = store.storeId }
            val purchaseId = purchaseDao.insert(purchase)
            for (item in items) item.purchaseId = purchaseId.also { item.isBought = true }
            itemDao.updateAll(items)
        }
    }

    fun findNearStores(cor: Coordinates, searchDist: Double): LiveData<List<Store>> {
        doAsync {
            val stores = storeDao.getNearStores(cor.sinLat, cor.cosLat, cor.sinLng, cor.cosLng, searchDist)
            uiThread { nearStores.value = stores }
        }
        return nearStores
    }

    fun getAllStores(): LiveData<List<Store>> {
        doAsync {
            val stores = storeDao.getAllSync()
            uiThread { allStores.value = stores }
        }
        return allStores
    }

    fun getPurchaseCountInPeriod(period: Int): LiveData<Int> {
        doAsync {
            val count = purchaseDao.getPurchaseCountInPeriod(period)
            uiThread { purchaseCount.value = count }
        }
        return purchaseCount
    }
}
