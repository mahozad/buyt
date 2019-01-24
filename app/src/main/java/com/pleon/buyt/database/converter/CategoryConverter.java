package com.pleon.buyt.database.converter;

import com.pleon.buyt.model.Item;

import androidx.room.TypeConverter;

public class CategoryConverter {

    @TypeConverter
    public static Item.Category convertToCategory(String name) {
        return Item.Category.valueOf(name);
    }

    @TypeConverter
    public static String convertToName(Item.Category category) {
        return category.name();
    }
}
