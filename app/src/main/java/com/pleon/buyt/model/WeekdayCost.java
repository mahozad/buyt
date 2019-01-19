package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.StringRes;

public class WeekdayCost {

    public enum Days {

        SUNDAY(R.string.sun),
        MONDAY(R.string.mon),
        TUESDAY(R.string.tue),
        WEDNESDAY(R.string.wed),
        THURSDAY(R.string.thu),
        FRIDAY(R.string.fri),
        SATURDAY(R.string.sat);

        @StringRes
        private int nameStringRes;

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
