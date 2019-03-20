package com.pleon.buyt.model

import androidx.annotation.StringRes
import com.pleon.buyt.R

private val internationalOrder = intArrayOf(1, 2, 3, 4, 5, 6, 0)
private val iranianOrder = intArrayOf(6, 0, 1, 2, 3, 4, 5) // to show days RTL reverse it

class DailyCost(val date: String, val totalCost: Long) {

    /**
     * According to international standard ISO 8601, Monday is the first day of the week.
     * It is followed by Tuesday, Wednesday, Thursday, Friday, and Saturday.
     * Sunday is the 7th and final day.
     *
     * Although this is the international standard, several countries, including
     * the United States, Canada, and Australia consider Sunday as the start of the week.
     */
    enum class Days constructor(@param:StringRes val nameStringRes: Int) {
        /**
         * Do NOT reorder the days. This is the order that is returned by sqlite (PurchaseDao).
         */
        SUNDAY(R.string.weekday_sunday),
        MONDAY(R.string.weekday_monday),
        TUESDAY(R.string.weekday_tuesday),
        WEDNESDAY(R.string.weekday_wednesday),
        THURSDAY(R.string.weekday_thursday),
        FRIDAY(R.string.weekday_friday),
        SATURDAY(R.string.weekday_saturday);
    }
}
