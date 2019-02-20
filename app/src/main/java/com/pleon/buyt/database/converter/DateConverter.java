package com.pleon.buyt.database.converter;

import java.util.Date;

import androidx.room.TypeConverter;

public class DateConverter {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value * 1_000); // multiply by 1000 to make it milliseconds
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime() / 1_000; // divide by 1000 to store as seconds
    }
}
