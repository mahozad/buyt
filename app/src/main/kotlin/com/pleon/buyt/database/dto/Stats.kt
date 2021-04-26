package com.pleon.buyt.database.dto

class Stats {
    lateinit var dailyCosts: List<DailyCost>
    lateinit var mostPurchasedCategories: List<CategorySum>
    var mostPurchasedItem: MostPurchasedItemDto? = null
    var storeWithMaxPurchaseCount: StoreWithMostPurchaseCountDto? = null
    var weekdayWithMaxPurchaseCount: WeekdayWithMostPurchaseCountDto? = null
    var totalPurchaseCount = 0L
    var totalPurchaseCost = 0L
    var maxPurchaseCost = 0L
    var minPurchaseCost = 0L
    var averagePurchaseCost = 0L
}
