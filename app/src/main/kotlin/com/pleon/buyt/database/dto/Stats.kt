package com.pleon.buyt.database.dto

import com.pleon.buyt.database.dto.DailyCost.WeekDay
import com.pleon.buyt.model.Store

class Stats {

    lateinit var dailyCosts: List<DailyCost>
    lateinit var mostPurchasedCategories: List<CategorySum>
    var mostPurchasedItem: MostPurchasedItemDto? = null
    var storeWithMaxPurchaseCount: Store? = null
    var numberOfPurchases = 0L
    var totalPurchaseCost = 0L
    var maxPurchaseCost = 0L
    var minPurchaseCost = 0L
    var averagePurchaseCost = 0L
    var weekdayWithMaxPurchases = 0
    val weekdayNameResWithMaxPurchases: Int?
        get() = if (numberOfPurchases != 0L) WeekDay.values()[weekdayWithMaxPurchases].nameStringRes else null

}
