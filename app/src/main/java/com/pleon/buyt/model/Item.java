package com.pleon.buyt.model;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * An item to buy.
 */
@Entity
public class Item {

    // model class attribute names don't need to start with 'm'

    @PrimaryKey(autoGenerate = true)
    private long id; // TODO: change type of id here to int?
    @ForeignKey(entity = Purchase.class, parentColumns = "id", childColumns = "purchaseId")
    private long purchaseId;

    // TODO: make fields final
    private String name;
    @Embedded
    private Quantity quantity;
    private String description;
    private boolean urgent;
    private boolean bought;
    private long price;

    @Ignore // for display purposes
    private boolean expanded;

    public Item(String name, Quantity quantity, boolean urgent, boolean bought) {
        this.name = name;
        this.quantity = quantity;
        this.urgent = urgent;
        this.bought = bought;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public boolean isBought() {
        return bought;
    }

    public void setBought(boolean bought) {
        this.bought = bought;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public void setQuantity(Quantity quantity) {
        this.quantity = quantity;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
