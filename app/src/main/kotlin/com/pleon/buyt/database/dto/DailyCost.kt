package com.pleon.buyt.database.dto

import androidx.annotation.StringRes
import com.pleon.buyt.R

class DailyCost(val date: String, val totalCost: Long) {

    /**
     * According to international standard ISO 8601, Monday is the first day of the week.
     * It is followed by Tuesday, Wednesday, Thursday, Friday, and Saturday.
     * Sunday is the 7th and final day.
     *
     * Although this is the international standard, several countries, including
     * the United States, Canada, and Australia consider Sunday as the start of the week.
     */
    enum class Days(@StringRes val nameStringRes: Int) {
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
