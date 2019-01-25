package com.pleon.buyt.database.converter;

import java.util.Date;

import androidx.room.TypeConverter;

public class DateConverter {

    @TypeConverter
    public static Date convertToDate(Long timestamp) { // the parameter should be boxed
        return new Date(timestamp);
    }

    @TypeConverter
    public static Long convertToTimestamp(Date date) {
        return date.getTime();
    }
}