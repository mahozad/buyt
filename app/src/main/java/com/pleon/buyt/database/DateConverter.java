package com.pleon.buyt.database;

import java.util.Date;

import androidx.room.TypeConverter;

public class DateConverter {

    @TypeConverter
    public static Date convertToDate(Long timestamp) { // the parameter should be object
        return new Date(timestamp);
    }

    @TypeConverter
    public static Long convertToTimestamp(Date date) {
        return date.getTime();
    }
}
