package com.pleon.buyt.model;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index({"latitude", "longitude"}))
public class Store {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Embedded
    private Coordinates location;

    private String name;
    private Category category;

    public Store(Coordinates location, String name, Category category) {
        this.location = location;
        this.name = name;
        this.category = category;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
