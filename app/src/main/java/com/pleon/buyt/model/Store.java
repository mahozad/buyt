package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index({"latitude", "longitude"}))
public class Store {

    // TODO: store the enum as a separate table in the database. see [https://softwareengineering.stackexchange.com/a/305153/311271]
    public enum Category {

        GENERIC(R.drawable.ic_store_generic),
        BAKERY(R.drawable.ic_store_bakery),
        BUTCHERY(R.drawable.ic_store_butchery),
        CHICKENERY(R.drawable.ic_store_chickenery),
        DAIRY(R.drawable.ic_store_dairy),
        FAST_FOOD(R.drawable.ic_store_fast_food),
        FISHERY(R.drawable.ic_store_fishery),
        FRUITERY(R.drawable.ic_store_fruitery),
        GROCERY(R.drawable.ic_store_grocery),
        HARDWARE(R.drawable.ic_store_hardwary),
        ICE_CREAMY(R.drawable.ic_store_ice_creamy),
        PASTRY(R.drawable.ic_store_pastry);

        private final int image;

        Category(int image) {
            this.image = image;
        }

        public int getImage() {
            return image;
        }
    }

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
