package com.pleon.buyt.model;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Item {

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
    private Category category;
    private long totalPrice;

    // for display purposes
    @Ignore
    private boolean expanded;
    private int position;
    // To fix the bug that happens when two items are deleted in row (the first appears again)
    private boolean flaggedForDeletion = false;

    public Item(String name, Quantity quantity, boolean urgent, boolean bought, Category category) {
        this.name = name;
        this.quantity = quantity;
        this.urgent = urgent;
        this.bought = bought;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isFlaggedForDeletion() {
        return flaggedForDeletion;
    }

    public void setFlaggedForDeletion(boolean flag) {
        this.flaggedForDeletion = flag;
    }
}
