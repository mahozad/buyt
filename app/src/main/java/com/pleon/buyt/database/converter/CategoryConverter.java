package com.pleon.buyt.database.converter;

import com.pleon.buyt.model.Item;
import com.pleon.buyt.model.Store;

import androidx.room.TypeConverter;

public class CategoryConverter {

    @TypeConverter
    public static Item.Category convertToItemCategory(String name) {
        return Item.Category.valueOf(name);
    }

    @TypeConverter
    public static String convertToItemCatName(Item.Category category) {
        return category.name();
    }

    @TypeConverter
    public static Store.Category convertToStoreCategory(String name) {
        return Store.Category.valueOf(name);
    }

    @TypeConverter
    public static String convertToStoreCatName(Store.Category category) {
        return category.name();
    }
}
