package com.pleon.buyt.database.dto

import androidx.room.Embedded
import androidx.room.Relation
import com.pleon.buyt.model.Item
import com.pleon.buyt.model.Purchase
import com.pleon.buyt.model.Store

class PurchaseDetail {

    @Embedded
    lateinit var purchase: Purchase

    @Relation(entityColumn = "purchaseId", parentColumn = "purchaseId")
    lateinit var item: List<Item>

    // The store may be null because user might have deleted it
    @Relation(entityColumn = "storeId", parentColumn = "storeId")
    var store: Store? = null
}
