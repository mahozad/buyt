package com.pleon.buyt.model

import androidx.room.Embedded
import androidx.room.Relation

class PurchaseDetail {

    @Embedded
    lateinit var purchase: Purchase

    @Relation(entityColumn = "purchaseId", parentColumn = "purchaseId")
    lateinit var item: List<Item>

    @Relation(entityColumn = "storeId", parentColumn = "storeId")
    lateinit var store: List<Store>
}
