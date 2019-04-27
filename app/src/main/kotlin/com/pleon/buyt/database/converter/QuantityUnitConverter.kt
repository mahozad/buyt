package com.pleon.buyt.database.converter

import androidx.room.TypeConverter
import com.pleon.buyt.model.Item.Quantity.Unit

class QuantityUnitConverter {

    @TypeConverter
    fun convertToUnit(name: String) = Unit.valueOf(name)

    @TypeConverter
    fun convertToName(unit: Unit) = unit.name
}
