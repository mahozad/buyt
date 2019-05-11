package com.pleon.buyt.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.pleon.buyt.database.SingleLiveEvent
import com.pleon.buyt.database.getDatabase
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

// A Repository class handles data operations. It provides a clean API to the rest of the app for app data
// A Repository manages query threads and allows you to use multiple back-ends.
// In the most common example, the Repository implements the logic for deciding whether
// to fetch data from a network or use results cached in a local database.
class MainRepository(application: Application) { // TODO: make this class singleton

    private val itemDao = getDatabase(application).itemDao()
    private val storeDao = getDatabase(application).storeDao()
    private val purchaseDao = getDatabase(application).purchaseDao()
    private val nearStores = SingleLiveEvent<List<Store>>()
    private val allStores = SingleLiveEvent<List<Store>>()
    val allItems = itemDao.getAll()

    fun updateItems(items: Collection<Item>) = doAsync { itemDao.updateAll(items) }

    fun deleteItem(item: Item) = doAsync { itemDao.delete(item) }

    fun buy(items: Collection<Item>, store: Store, purchaseDate: Date) {
        doAsync {
            val purchase = Purchase(store.storeId, purchaseDate)
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
}
