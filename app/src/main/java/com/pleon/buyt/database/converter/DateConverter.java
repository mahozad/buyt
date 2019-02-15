package com.pleon.buyt.database.converter;

import java.util.Date;

import androidx.room.TypeConverter;

public class DateConverter {

    @TypeConverter
    public static Date convertToDate(Long timestamp) {
        return new Date(timestamp * 1000); // multiply by 1000 to make it milliseconds
    }

    @TypeConverter
    public static Long convertToTimestamp(Date date) {
        return date.getTime() / 1000; // divide by 1000 to store as seconds
    }
}
