package com.yeraygarcia.recipes.database

import android.arch.persistence.room.TypeConverter
import java.util.*


object DateTypeConverter {

    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
