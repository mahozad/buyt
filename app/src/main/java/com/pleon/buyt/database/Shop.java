package com.pleon.buyt.database;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Shop {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @Embedded
    public Coordinates location;

    public String name;
    public String category;

    public Shop(Coordinates location, String name, String category) {
        this.location = location;
        this.name = name;
        this.category = category;
    }
}
