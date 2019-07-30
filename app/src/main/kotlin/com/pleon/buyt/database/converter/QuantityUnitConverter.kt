package com.pleon.buyt.database.converter

import androidx.room.TypeConverter
import com.pleon.buyt.model.Item.Quantity

class QuantityUnitConverter {

    @TypeConverter
    fun convertToUnit(name: String) = Quantity.Unit.valueOf(name)

    @TypeConverter
    fun convertToName(unit: Quantity.Unit) = unit.name
}
