package com.pleon.buyt.util

import java.text.NumberFormat

object NumberFormatUtil {

    // FIXME: For improving performance, instead of calling getInstance in every invocation of the
    //  following methods, convert the getInstance to a field in the class and then when the user
    //  changes the language, call getInstance again to get a format matching the new locale

    fun formatPrice(price: Int) = formatPrice(price.toLong())
    fun formatPrice(price: String) = formatPrice(price.toLong())
    fun formatPrice(price: Long): String = NumberFormat.getNumberInstance().format(price)

    fun formatNumber(number: Int): String = formatNumber(number.toLong())
    fun formatNumber(number: Long): String = NumberFormat.getNumberInstance().format(number)

    fun formatPercent(fraction: Float): String = formatPercent(fraction.toDouble())
    fun formatPercent(fraction: Double): String = NumberFormat.getPercentInstance().format(fraction)
}
