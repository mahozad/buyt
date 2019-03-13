package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.StringRes;

public class DailyCost {

    /**
     * According to international standard ISO 8601, Monday is the first day of the week.
     * It is followed by Tuesday, Wednesday, Thursday, Friday, and Saturday.
     * Sunday is the 7th and final day.
     * <p>
     * Although this is the international standard, several countries, including
     * the United States, Canada, and Australia consider Sunday as the start of the week.
     */
    public enum Days {

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

        private int nameStringRes;
        public static int[] internationalOrder = {1, 2, 3, 4, 5, 6, 0};
        public static int[] iranianOrder = {6, 0, 1, 2, 3, 4, 5}; // to show days RTL reverse it

        Days(@StringRes int nameStringRes) {
            this.nameStringRes = nameStringRes;
        }

        public int getNameStringRes() {
            return nameStringRes;
        }
    }

    private final String date;
    private final long totalCost;

    public DailyCost(String date, long totalCost) {
        this.date = date;
        this.totalCost = totalCost;
    }

    public String getDate() {
        return date;
    }

    public long getTotalCost() {
        return totalCost;
    }
}
