package com.pleon.buyt.repository

import androidx.lifecycle.LiveData
import com.pleon.buyt.database.SingleLiveEvent
import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddItemRepository @Inject constructor(private val itemDao: ItemDao,
                                            private val purchaseDao: PurchaseDao) {

    private val itemNameCats = SingleLiveEvent<Map<String, String>>()

    fun addItem(item: Item) = doAsync { itemDao.insert(item) }

    fun addPurchasedItem(item: Item, store: Store, purchaseDate: Date) {
        doAsync {
            val purchase = Purchase(store.storeId, purchaseDate)
            purchaseDao.insert(purchase).also { item.purchaseId = it }
            itemDao.insert(item)
        }
    }

    fun getItemNameCats(): LiveData<Map<String, String>> {
        doAsync {
            val nameCats = itemDao.getItemNamesAndCats()
            uiThread { itemNameCats.value = nameCats }
        }
        return itemNameCats
    }
}
