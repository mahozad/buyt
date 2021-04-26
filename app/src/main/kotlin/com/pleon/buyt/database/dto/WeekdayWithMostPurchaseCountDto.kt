package com.pleon.buyt.database.dto

import androidx.annotation.StringRes

data class WeekdayWithMostPurchaseCountDto(val weekday: Int, val purchaseCount: Int) {
    @get:StringRes
    val weekdayNameStringRes: Int
        get() = DailyCost.WeekDay.values()[weekday].nameStringRes
}
