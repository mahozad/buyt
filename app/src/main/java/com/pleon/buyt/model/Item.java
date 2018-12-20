package com.pleon.buyt.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * An item to buy.
 */
@Entity
public class Item {

    //  model class attribute names don't need to start with 'm'

    @PrimaryKey(autoGenerate = true)
    private long id; // TODO: change type of id here to int

    @ForeignKey(entity = Purchase.class, parentColumns = "id", childColumns = "purchaseId")
    private long purchaseId;

    private final String name;
    private final String price;
    private String description;
    private String category;
    private double volume;
    private boolean bought;
    private boolean urgent;

    @Ignore // for display purposes
    private boolean expanded;
    @Ignore // for display purposes
    private boolean selected;

    public Item(String name, String price, double volume, String category) {
        this.name = name;
        this.price = price;
        this.volume = volume;
        this.category = category;
        description = "I am a description set in the constructor";
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

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
