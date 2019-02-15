package com.pleon.buyt.model;

import java.util.List;

public class Statistics {

    private long totalPurchaseCost;
    private Category mostPurchasedCategory;
    private long averagePurchaseCost;
    private Store storeWithMaxPurchases;
    private long maxPurchaseCost;
    private long minPurchaseCost;
    private List<DailyCost> dailyCosts;

    public long getAveragePurchaseCost() {
        return averagePurchaseCost;
    }

    public void setAveragePurchaseCost(long averagePurchaseCost) {
        this.averagePurchaseCost = averagePurchaseCost;
    }

    public long getTotalPurchaseCost() {
        return totalPurchaseCost;
    }

    public void setTotalPurchaseCost(long totalPurchaseCost) {
        this.totalPurchaseCost = totalPurchaseCost;
    }

    public Category getMostPurchasedCategory() {
        return mostPurchasedCategory;
    }

    public void setMostPurchasedCategory(Category category) {
        this.mostPurchasedCategory = category;
    }

    public List<DailyCost> getDailyCosts() {
        return dailyCosts;
    }

    public void setDailyCosts(List<DailyCost> dailyCosts) {
        this.dailyCosts = dailyCosts;
    }

    public long getMaxPurchaseCost() {
        return maxPurchaseCost;
    }

    public void setMaxPurchaseCost(long maxPurchaseCost) {
        this.maxPurchaseCost = maxPurchaseCost;
    }

    public Store getStoreWithMaxPurchases() {
        return storeWithMaxPurchases;
    }

    public void setStoreWithMaxPurchases(Store storeWithMaxPurchases) {
        this.storeWithMaxPurchases = storeWithMaxPurchases;
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "totalPurchaseCost=" + totalPurchaseCost +
                ", mostPurchasedCategory=" + mostPurchasedCategory +
                ", averagePurchaseCost=" + averagePurchaseCost +
                ", storeWithMaxPurchases=" + storeWithMaxPurchases.getStoreId() +
                ", maxPurchaseCost=" + maxPurchaseCost +
                ", minPurchaseCost=" + minPurchaseCost +
                '}';
    }
}
