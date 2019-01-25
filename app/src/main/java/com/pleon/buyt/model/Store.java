package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = @Index({"latitude", "longitude"}))
public class Store {

    // TODO: store the enum as a separate table in the database. see [https://softwareengineering.stackexchange.com/a/305153/311271]
    public enum Category {

        GENERIC(R.string.store_cat_generic, R.drawable.ic_store_generic),
        GROCERY_STORE(R.string.store_cat_grocery_store, R.drawable.ic_store_grocery),
        BAKERY(R.string.store_cat_bakery, R.drawable.ic_store_bakery),
        BUTCHERY(R.string.store_cat_butchery, R.drawable.ic_store_butchery),
        CHICKEN_SHOP(R.string.store_cat_chicken_shop, R.drawable.ic_store_chickenery),
        DAIRY(R.string.store_cat_dairy, R.drawable.ic_store_dairy),
        FAST_FOOD(R.string.store_cat_fast_food, R.drawable.ic_store_fast_food),
        FISH_SHOP(R.string.store_cat_fish_shop, R.drawable.ic_store_fishery),
        GROCERS(R.string.store_cat_grocers, R.drawable.ic_store_fruitery),
        HARDWARE_STORE(R.string.store_cat_hardware_store, R.drawable.ic_store_hardwary),
        ICE_CREAM_SHOP(R.string.store_cat_icecream_shop, R.drawable.ic_store_ice_creamy),
        DRUG_STORE(R.string.store_cat_drugstore, R.drawable.ic_store_ice_creamy),
        FLOWER_SHOP(R.string.store_cat_flower_shop, R.drawable.ic_store_ice_creamy),
        DRY_CLEANERS(R.string.store_cat_dry_cleaners, R.drawable.ic_store_ice_creamy),
        STATIONARY_STORE(R.string.store_cat_stationary_store, R.drawable.ic_store_ice_creamy),
        BOOKSHOP(R.string.store_cat_book_shop, R.drawable.ic_store_ice_creamy),
        CLOTHES_SHOP(R.string.store_cat_clothes_shop, R.drawable.ic_store_ice_creamy),
        SHOE_SHOP(R.string.store_cat_shoe_shop, R.drawable.ic_store_ice_creamy),
        CONFECTIONERY(R.string.store_cat_confectionery, R.drawable.ic_store_pastry);

        @StringRes private final int name;
        @DrawableRes private final int image;

        Category(int name, int image) {
            this.image = image;
            this.name = name;
        }

        public int getNameRes() {
            return name;
        }

        public int getImageRes() {
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
