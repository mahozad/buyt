package com.pleon.buyt.model;

import java.text.NumberFormat;
import java.util.List;

public class Statistics {

    private List<DailyCost> dailyCosts;
    private long totalPurchaseCost;
    private long averagePurchaseCost;
    private Category mostPurchasedCategory;
    private int numberOfPurchases;
    private long maxPurchaseCost;
    private long minPurchaseCost;
    private int weekdayWithMaxPurchases;
    private Store storeWithMaxPurchaseCount;

    public List<DailyCost> getDailyCosts() {
        return dailyCosts;
    }

    public void setDailyCosts(List<DailyCost> dailyCosts) {
        this.dailyCosts = dailyCosts;
    }

    public String getTotalPurchaseCost() {
        return NumberFormat.getInstance().format(totalPurchaseCost);
    }

    public void setTotalPurchaseCost(long totalPurchaseCost) {
        this.totalPurchaseCost = totalPurchaseCost;
    }

    public String getAveragePurchaseCost() {
        return NumberFormat.getInstance().format(averagePurchaseCost);
    }

    public void setAveragePurchaseCost(long averagePurchaseCost) {
        this.averagePurchaseCost = averagePurchaseCost;
    }

    public int getMostPurchasedCategoryName() {
        return mostPurchasedCategory.getNameRes();
    }

    public void setMostPurchasedCategory(Category mostPurchasedCategory) {
        this.mostPurchasedCategory = mostPurchasedCategory;
    }

    public String getNumberOfPurchases() {
        return NumberFormat.getInstance().format(numberOfPurchases);
    }

    public void setNumberOfPurchases(int numberOfPurchases) {
        this.numberOfPurchases = numberOfPurchases;
    }

    public String getMaxPurchaseCost() {
        return NumberFormat.getInstance().format(maxPurchaseCost);
    }

    public void setMaxPurchaseCost(long maxPurchaseCost) {
        this.maxPurchaseCost = maxPurchaseCost;
    }

    public String getMinPurchaseCost() {
        return NumberFormat.getInstance().format(minPurchaseCost);
    }

    public void setMinPurchaseCost(long minPurchaseCost) {
        this.minPurchaseCost = minPurchaseCost;
    }

    public int getWeekdayNameResWithMaxPurchases() {
        return DailyCost.Days.values()[weekdayWithMaxPurchases].getNameStringRes();
    }

    public void setWeekdayWithMaxPurchases(int weekdayWithMaxPurchases) {
        this.weekdayWithMaxPurchases = weekdayWithMaxPurchases;
    }

    public String getStoreNameWithMaxPurchaseCount() {
        return storeWithMaxPurchaseCount.getName();
    }

    public void setStoreWithMaxPurchaseCount(Store storeWithMaxPurchases) {
        this.storeWithMaxPurchaseCount = storeWithMaxPurchases;
    }
}
