package com.pleon.buyt.model;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity
public class Purchase {

    @PrimaryKey(autoGenerate = true)
    private long purchaseId;

    @ForeignKey(entity = Store.class, parentColumns = "id", childColumns = "storeId")
    private long storeId;

    private long totalCost;

    private Date date;

    public Purchase(long storeId, Date date, long totalCost) {
        this.storeId = storeId;
        this.date = date;
        this.totalCost = totalCost;
    }

    public long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }
}
