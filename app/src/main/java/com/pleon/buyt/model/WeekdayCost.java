package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.StringRes;

public class WeekdayCost {

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
        SUNDAY(R.string.sun),
        MONDAY(R.string.mon),
        TUESDAY(R.string.tue),
        WEDNESDAY(R.string.wed),
        THURSDAY(R.string.thu),
        FRIDAY(R.string.fri),
        SATURDAY(R.string.sat);

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

    private int day;
    private long cost;

    public WeekdayCost(int day, long cost) {
        this.day = day;
        this.cost = cost;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }
}
