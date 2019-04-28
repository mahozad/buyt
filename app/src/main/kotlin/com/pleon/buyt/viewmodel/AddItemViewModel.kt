package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.AddItemRepository
import java.util.*

class AddItemViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AddItemRepository(application)
    val itemNameCats get() = repository.getItemNameCats()
    var category = GROCERY
    var storeList: List<Store>? = null
    var store: Store? = null
    var purchaseDate = Date()

    fun addItem(item: Item, isPurchased: Boolean) {
        if (isPurchased) repository.addPurchasedItem(item, store!!, purchaseDate)
        else repository.addItem(item)
    }
}
