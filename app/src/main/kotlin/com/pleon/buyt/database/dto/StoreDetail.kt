package com.pleon.buyt.database.dto

import androidx.room.Embedded
import com.pleon.buyt.model.Store

data class StoreDetail(@Embedded var store: Store, var purchaseCount: Int, var totalSpending: Int)
