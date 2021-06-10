package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pleon.buyt.R
import com.pleon.buyt.database.dto.ItemNameCat
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.AddItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.*

class AddItemViewModel(app: Application, val repository: AddItemRepository) : AndroidViewModel(app) {

    init {
        viewModelScope.launch {
            storeList = repository.getAllStores()
        }
    }

    val defaultQuantity = 1L
    val defaultInitialItemCategory = GROCERY
    lateinit var storeList: List<Store>
    var category = GROCERY
    var purchaseDate = Date()
    var store: Store? = null

    private val defaultNameCats = flow {
        fun String.toNameCat() =
            ItemNameCat(substringBefore(":"), Category.valueOf(substringAfter(":")))
        app.resources.openRawResource(R.raw.item_names)
            .reader()
            .useLines { lines ->
                emit(lines.map { it.toNameCat() }.toList())
            }
    }.flowOn(Dispatchers.IO)

    /**
     * Could also have written it like this:
     * ```
     * val x = flow1.combine(flow2) { f1, f2 -> ... }
     * ```
     */
    val itemNameCats = combine(defaultNameCats, repository.itemNameCats) { default, database ->
        (default + database).associateBy({ it.name }, { it.category })
    }.flowOn(Dispatchers.Default)

    fun addItem(item: Item, isBought: Boolean) = viewModelScope.launch {
        if (isBought) {
            repository.addPurchasedItem(item, store!!, purchaseDate)
        } else {
            repository.addItem(item)
        }
    }

    fun resetValues() {
        purchaseDate = Date()
        category = defaultInitialItemCategory
        store = null
    }
}
