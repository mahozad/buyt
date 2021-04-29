package com.pleon.buyt.database.dto

import androidx.room.Embedded
import com.pleon.buyt.model.Store

class StoreDetail {
    data class StoreBrief(@Embedded var store: Store, var purchaseCount: Int, var totalSpending: Int)

    // Show these in UI too!
    data class PurchaseSummary(
            var maxPurchaseCost: Long = 0L,
            var avgPurchaseCost: Long = 0L,
            var minPurchaseCost: Long = 0L
    )

    lateinit var brief: StoreBrief
    lateinit var dailyCosts: List<DailyCost>
    lateinit var purchaseSummary: PurchaseSummary
}
