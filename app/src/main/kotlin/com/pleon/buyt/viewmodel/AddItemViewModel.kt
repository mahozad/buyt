package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.pleon.buyt.R
import com.pleon.buyt.model.Category.GROCERY
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Store
import com.pleon.buyt.repository.AddItemRepository
import java.io.InputStreamReader
import java.util.*
import javax.inject.Inject

class AddItemViewModel @Inject constructor(app: Application, val repository: AddItemRepository)
    : AndroidViewModel(app) {

    private val nameCatsMediator = MediatorLiveData<Map<String, String>>()
    val itemNameCats: LiveData<Map<String, String>> = nameCatsMediator
    var category = GROCERY
    var storeList: List<Store>? = null
    var store: Store? = null
    var purchaseDate = Date()

    init {
        nameCatsMediator.addSource(repository.getItemNameCats()) { dbNameCats ->
            // Do NOT reorder the operands in the following + operation
            nameCatsMediator.value = defaultItemNameCats + dbNameCats
        }
    }

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
