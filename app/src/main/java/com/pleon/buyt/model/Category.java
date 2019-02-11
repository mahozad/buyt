package com.pleon.buyt.model;

import com.pleon.buyt.R;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

// TODO: store the enum as a separate table in the database. see [https://softwareengineering.stackexchange.com/a/305153/311271]
public enum Category {

    GROCERY(R.string.item_cat_grocery, R.string.store_cat_grocery_store, R.drawable.ic_item_grocery, R.drawable.ic_store_grocery),
    BREAD(R.string.item_cat_bread, R.string.store_cat_bakery, R.drawable.ic_item_bread, R.drawable.ic_store_bakery),
    DAIRY(R.string.item_cat_dairy, R.string.store_cat_dairy, R.drawable.ic_item_dairy, R.drawable.ic_store_dairy),
    FRUIT(R.string.item_cat_fruit, R.string.store_cat_grocers, R.drawable.ic_item_fruit, R.drawable.ic_store_fruitery),
    MEAT(R.string.item_cat_meat, R.string.store_cat_butchery, R.drawable.ic_item_meat, R.drawable.ic_store_butchery),
    CHICKEN(R.string.item_cat_chicken, R.string.store_cat_chicken_shop, R.drawable.ic_item_chicken, R.drawable.ic_store_chickenery),
    FISH(R.string.item_cat_fish, R.string.store_cat_fish_shop, R.drawable.ic_item_fishery, R.drawable.ic_store_fishery),
    FAST_FOOD(R.string.item_cat_fast_food, R.string.store_cat_fast_food, R.drawable.ic_item_fast_food, R.drawable.ic_store_fast_food),
    ICE_CREAM(R.string.item_cat_ice_cream, R.string.store_cat_icecream_shop, R.drawable.ic_item_icecream, R.drawable.ic_store_ice_creamy),
    PASTRY(R.string.item_cat_pastry, R.string.store_cat_confectionery, R.drawable.ic_item_pastry, R.drawable.ic_store_pastry),
    //    VEGETABLE(R.string.item_cat_vegetable, R.drawable.ic_item_vegetable),
    //    DRUG_STORE(R.string.store_cat_drugstore, R.drawable.ic_store_ice_creamy),
    //    FLOWER_SHOP(R.string.store_cat_flower_shop, R.drawable.ic_store_ice_creamy),
    //    DRY_CLEANERS(R.string.store_cat_dry_cleaners, R.drawable.ic_store_ice_creamy),
    //    STATIONARY_STORE(R.string.store_cat_stationary_store, R.drawable.ic_store_ice_creamy),
    //    BOOKSHOP(R.string.store_cat_book_shop, R.drawable.ic_store_ice_creamy),
    //    CLOTHES_SHOP(R.string.store_cat_clothes_shop, R.drawable.ic_store_ice_creamy),
    //    SHOE_SHOP(R.string.store_cat_shoe_shop, R.drawable.ic_store_ice_creamy);
    //    OTHER(R.string.item_cat_other, R.drawable.ic_item_other),
    TOOLS(R.string.item_cat_tools, R.string.store_cat_hardware_store, R.drawable.ic_item_tools, R.drawable.ic_store_hardwary);

    @StringRes private final int name;
    @StringRes private final int storeName;
    @DrawableRes private final int image;
    @DrawableRes private final int storeImage;

    Category(int name, int storeName, int image, int storeImage) {
        this.name = name;
        this.storeName = storeName;
        this.image = image;
        this.storeImage = storeImage;
    }

    public int getNameRes() {
        return name;
    }

    public int getStoreNameRes() {
        return storeName;
    }

    public int getImageRes() {
        return image;
    }

    public int getStoreImageRes() {
        return storeImage;
    }
}
