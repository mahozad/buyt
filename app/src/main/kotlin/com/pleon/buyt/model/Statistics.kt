package com.pleon.buyt.model

import com.pleon.buyt.database.DailyCost
import com.pleon.buyt.database.PieSlice
import java.text.NumberFormat

class Statistics {

    var dailyCosts: List<DailyCost>? = null
    var totalPurchaseCost: Long = 0
    var averagePurchaseCost: Long = 0
    var numberOfPurchases: Int = 0
    var maxPurchaseCost: Long = 0
    var minPurchaseCost: Long = 0
    lateinit var mostPurchasedCategories: List<PieSlice>
    private var weekdayWithMaxPurchases: Int = 0
    private var storeWithMaxPurchaseCount: Store? = null

    val weekdayNameResWithMaxPurchases: Int
        get() {
            return if (totalPurchaseCost != 0L) DailyCost.Days.values()[weekdayWithMaxPurchases].nameStringRes
            else 0
        }

    val storeNameWithMaxPurchaseCount: String
        get() = if (storeWithMaxPurchaseCount == null) "-" else storeWithMaxPurchaseCount!!.name

    fun getTotalPurchaseCost(): String {
        return NumberFormat.getInstance().format(totalPurchaseCost)
    }

    fun getAveragePurchaseCost(): String {
        return NumberFormat.getInstance().format(averagePurchaseCost)
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

    fun setStoreWithMaxPurchaseCount(storeWithMaxPurchases: Store?) {
        this.storeWithMaxPurchaseCount = storeWithMaxPurchases
    }
}
