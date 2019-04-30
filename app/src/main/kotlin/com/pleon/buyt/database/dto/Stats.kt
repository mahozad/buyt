package com.pleon.buyt.database.dto

import com.pleon.buyt.model.Store

class Stats {

    var dailyCosts: List<DailyCost>? = null
    var storeWithMaxPurchaseCount: Store? = null
    var totalPurchaseCost = 0L
    var averagePurchaseCost = 0L
    var maxPurchaseCost = 0L
    var minPurchaseCost = 0L
    var numberOfPurchases: Int = 0
    var weekdayWithMaxPurchases: Int = 0
    lateinit var mostPurchasedCategories: List<PieSlice>
    lateinit var purchaseDetails: List<PurchaseDetail>

    val weekdayNameResWithMaxPurchases: Int
        get() =
            if (totalPurchaseCost != 0L) DailyCost.Days.values()[weekdayWithMaxPurchases].nameStringRes
            else 0

    val storeNameWithMaxPurchaseCount: String
        get() = if (storeWithMaxPurchaseCount == null) "-" else storeWithMaxPurchaseCount!!.name
}
