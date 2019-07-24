package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.arch.core.util.Function
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations.map
import com.pleon.buyt.R
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.AddItemRepository
import java.io.InputStreamReader
import java.util.*

class AddItemViewModel constructor(app: Application, val repository: AddItemRepository)
    : AndroidViewModel(app) {

    var category = GROCERY
    var store: Store? = null
    var storeList: List<Store>? = null
    var purchaseDate = Date()
    val allStores get() = repository.getAllStores()
    val itemNameCats: LiveData<Map<String, String>> = map(repository.itemNameCats, Function { dbNameCats ->
        return@Function defaultItemNameCats + dbNameCats.associateBy({ it.name }, { it.category })
    })

    private val defaultItemNameCats by lazy {
        InputStreamReader(app.resources.openRawResource(R.raw.item_names)).readLines()
                .associateBy({ it.substringBefore(':') }, { it.substringAfter(':') })
                .toMutableMap()
    }

    fun addItem(item: Item, isPurchased: Boolean) {
        if (isPurchased) repository.addPurchasedItem(item, store!!, purchaseDate)
        else repository.addItem(item)
    }
}
