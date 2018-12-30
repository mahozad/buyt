package com.pleon.buyt.model;

import java.io.Serializable;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index({"latitude", "longitude"}))
public class Store implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Embedded
    private Coordinates location;

    private String name;
    private String category;

    public Store(Coordinates location, String name, String category) {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
