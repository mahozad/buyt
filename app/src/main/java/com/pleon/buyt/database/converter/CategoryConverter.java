package com.pleon.buyt.database.converter;

import com.pleon.buyt.model.Category;

import androidx.room.TypeConverter;

public class CategoryConverter {

    @TypeConverter
    public static Category convertToItemCategory(String name) {
        return Category.valueOf(name);
    }

    @TypeConverter
    public static String convertToItemCategoryName(Category category) {
        return category != null ? category.name() : null;
    }
}
