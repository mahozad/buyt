package com.pleon.buyt.database.dto

import androidx.room.Embedded
import com.pleon.buyt.model.Store

class StoreDetail {

    @Embedded
    lateinit var store: Store

    var purchaseCount: Int = 0
    var totalSpending: Int = 0
}
