package com.pleon.buyt.database.repository

import android.app.Application
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import org.jetbrains.anko.doAsync
import java.util.*

class AddItemRepository(application: Application) {

    private val itemDao = AppDatabase.getDatabase(application).itemDao()
    private val purchaseDao = AppDatabase.getDatabase(application).purchaseDao()
    val itemNames = itemDao.getItemNames()

    fun addItem(item: Item) = doAsync { itemDao.insert(item) }

    fun addPurchasedItem(item: Item, store: Store, purchaseDate: Date) {
        doAsync {
            val purchase = Purchase(store.storeId, purchaseDate)
            purchaseDao.insert(purchase).also { item.purchaseId = it }
            itemDao.insert(item)
        }
    }
}
