package com.pleon.buyt.model;

import java.util.List;

public class Statistics {

    private long averagePurchaseCost;
    private long totalPurchaseCost;
    private long maxPurchaseCost;
    private long minPurchaseCost;
    private Category category;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<DailyCost> getDailyCosts() {
        return dailyCosts;
    }

    public void setDailyCosts(List<DailyCost> dailyCosts) {
        this.dailyCosts = dailyCosts;
    }
}
