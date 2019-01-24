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

        GROCERY(R.drawable.ic_item_generic),
        BAKERY(R.drawable.ic_item_bread),
        BUTCHERY(R.drawable.ic_item_meat),
        MEAT(R.drawable.ic_item_meat);

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
