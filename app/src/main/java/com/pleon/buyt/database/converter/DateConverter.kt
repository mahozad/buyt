package com.pleon.buyt.database.converter

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value * 1000) // multiply by 1000 to make it milliseconds
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        return if (date == null) null else date.time / 1000 // divide by 1000 to store as seconds
    }
}
