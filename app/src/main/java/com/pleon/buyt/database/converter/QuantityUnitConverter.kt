package com.pleon.buyt.database.converter

import androidx.room.TypeConverter
import com.pleon.buyt.model.Quantity.Unit

class QuantityUnitConverter {

    @TypeConverter
    fun convertToUnit(name: String): Unit {
        return Unit.valueOf(name)
    }

    @TypeConverter
    fun convertToName(unit: Unit): String {
        return unit.name
    }
}
