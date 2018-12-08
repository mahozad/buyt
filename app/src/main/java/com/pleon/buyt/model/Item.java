package com.pleon.buyt.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * An item_list_row to buy.
 */
@Entity
public class Item {

//    model classes attributes don't need to start with 'm'

    @PrimaryKey(autoGenerate = true)
    private long id; // TODO: change type of id here to int

    @ForeignKey(entity = Purchase.class, parentColumns = "id", childColumns = "purchaseId")
    private long purchaseId;

    private final String name;
    private final String price;
    private double volume;
    private boolean bought;
    private String category;

    public Item(String name, String price, double volume, String category) {
        this.name = name;
        this.price = price;
        this.volume = volume;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
