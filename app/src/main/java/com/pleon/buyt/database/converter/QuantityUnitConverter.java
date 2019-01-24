package com.pleon.buyt.database.converter;

import com.pleon.buyt.model.Quantity.Unit;

import androidx.room.TypeConverter;

public class QuantityUnitConverter {

    @TypeConverter
    public static Unit convertToUnit(String name) {
        return Unit.valueOf(name);
    }

    @TypeConverter
    public static String convertToName(Unit unit) {
        return unit.name();
    }
}
