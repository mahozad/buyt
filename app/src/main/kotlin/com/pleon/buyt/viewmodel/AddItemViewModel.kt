package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.pleon.buyt.R
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.AddItemRepository
import java.io.InputStreamReader
import java.util.*

class AddItemViewModel(app: Application, val repository: AddItemRepository) : AndroidViewModel(app) {

    var category = GROCERY
    var purchaseDate = Date()
    var store: Store? = null
    var storeList: List<Store>? = null
    val allStores get() = repository.getAllStores()
    val itemNameCats: LiveData<Map<String, Category>> = map(repository.itemNameCats, Function { dbNameCats ->
        return@Function defaultNameCats + dbNameCats.associateBy({ it.name }, { cat(it.category) })
    })

    private val defaultNameCats by lazy {
        InputStreamReader(app.resources.openRawResource(R.raw.item_names)).readLines()
                .associateBy({ it.substringBefore(':') }, { cat(it.substringAfter(':')) })
    }

    private fun cat(catName: String) = Category.valueOf(catName)

    fun addItem(item: Item, isPurchased: Boolean) {
        if (isPurchased) repository.addPurchasedItem(item, store!!, purchaseDate)
        else repository.addItem(item)
    }
}
