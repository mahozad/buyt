package com.pleon.buyt.database.converter

import androidx.room.TypeConverter
import com.pleon.buyt.model.Category

class CategoryConverter {

    @TypeConverter
    fun convertToItemCategory(name: String) = Category.valueOf(name)

    @TypeConverter
    fun convertToItemCategoryName(category: Category?) = category?.name
}
