package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Item {

    // TODO: store the enum as a separate table in the database. see [https://softwareengineering.stackexchange.com/a/305153/311271]
    public enum Category {

        GROCERY(R.string.item_cat_grocery, R.drawable.ic_item_generic),
        BREAD(R.string.item_cat_bread, R.drawable.ic_item_bread),
        DAIRY(R.string.item_cat_dairy, R.drawable.ic_item_dairy),
        FRUIT(R.string.item_cat_fruit, R.drawable.ic_item_fruit),
        VEGETABLE(R.string.item_cat_vegetable, R.drawable.ic_item_vegetable),
        MEAT(R.string.item_cat_meat, R.drawable.ic_item_meat),
        CHICKEN(R.string.item_cat_chicken, R.drawable.ic_item_chicken),
        FISH(R.string.item_cat_fish, R.drawable.ic_item_fishery),
        FAST_FOOD(R.string.item_cat_fast_food, R.drawable.ic_item_fast_food),
        ICE_CREAM(R.string.item_cat_ice_cream, R.drawable.ic_item_icecream),
        PASTRY(R.string.item_cat_pastry, R.drawable.ic_item_pastry),
        TOOLS(R.string.item_cat_tools, R.drawable.ic_item_tools),
        OTHER(R.string.item_cat_other, R.drawable.ic_item_other);

        @StringRes private final int name;
        @DrawableRes private final int image;

        Category(int name, int image) {
            this.name = name;
            this.image = image;
        }

        public int getNameRes() {
            return name;
        }

        public int getImageRes() {
            return image;
        }
    }

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
