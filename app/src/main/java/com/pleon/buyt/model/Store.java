package com.pleon.buyt.model;

import java.io.Serializable;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index({"latitude", "longitude"}))
public class Store implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long storeId;

    @Embedded
    private Coordinates location;

    private String name;
    private Category category;
    // To fix the bug that happens when two stores are deleted in a row (the first appears again)
    private boolean flaggedForDeletion = false;

    public Store(Coordinates location, String name, Category category) {
        this.location = location;
        this.name = name;
        this.category = category;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public Coordinates getLocation() {
        return location;
    }

    public void setLocation(Coordinates location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public boolean isFlaggedForDeletion() {
        return flaggedForDeletion;
    }

    public void setFlaggedForDeletion(boolean flag) {
        this.flaggedForDeletion = flag;
    }
}
