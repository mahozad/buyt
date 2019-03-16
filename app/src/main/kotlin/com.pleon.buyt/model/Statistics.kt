package com.pleon.buyt.model

import java.text.NumberFormat

class Statistics {

    var dailyCosts: List<DailyCost>? = null
    var totalPurchaseCost: Long = 0
    var averagePurchaseCost: Long = 0
    private var mostPurchasedCategory: Category? = null
    var numberOfPurchases: Int = 0
    var maxPurchaseCost: Long = 0
    var minPurchaseCost: Long = 0
    private var weekdayWithMaxPurchases: Int = 0
    private var storeWithMaxPurchaseCount: Store? = null

    val mostPurchasedCategoryName: Int
        get() = if (mostPurchasedCategory == null) 0 else mostPurchasedCategory!!.nameRes

    val weekdayNameResWithMaxPurchases: Int
        get() = DailyCost.Days.values()[weekdayWithMaxPurchases].nameStringRes

    val storeNameWithMaxPurchaseCount: String
        get() = if (storeWithMaxPurchaseCount == null) "" else storeWithMaxPurchaseCount!!.name!!

    fun getTotalPurchaseCost(): String {
        return NumberFormat.getInstance().format(totalPurchaseCost)
    }


    fun getAveragePurchaseCost(): String {
        return NumberFormat.getInstance().format(averagePurchaseCost)
    }


    fun setMostPurchasedCategory(mostPurchasedCategory: Category) {
        this.mostPurchasedCategory = mostPurchasedCategory
    }

    fun getNumberOfPurchases(): String {
        return NumberFormat.getInstance().format(numberOfPurchases.toLong())
    }

    fun getMaxPurchaseCost(): String {
        return NumberFormat.getInstance().format(maxPurchaseCost)
    }

    fun getMinPurchaseCost(): String {
        return NumberFormat.getInstance().format(minPurchaseCost)
    }

    fun setWeekdayWithMaxPurchases(weekdayWithMaxPurchases: Int) {
        this.weekdayWithMaxPurchases = weekdayWithMaxPurchases
    }

    fun setStoreWithMaxPurchaseCount(storeWithMaxPurchases: Store) {
        this.storeWithMaxPurchaseCount = storeWithMaxPurchases
    }
}
