package com.pleon.buyt.database.converter

import androidx.room.TypeConverter
import java.util.*

class DateConverter {

    /**
     * multiply by 1000 to make it in milliseconds
     */
    @TypeConverter
    fun fromTimestamp(value: Long?) = if (value == null) null else Date(value * 1000)

    /**
     * divide by 1000 to store as seconds
     */
    @TypeConverter
    fun toTimestamp(date: Date?) = if (date == null) null else date.time / 1000
}
