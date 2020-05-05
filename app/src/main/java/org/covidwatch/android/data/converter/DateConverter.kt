package org.covidwatch.android.data.converter

import androidx.room.TypeConverter
import java.util.Date

class DateConverter {
    @TypeConverter
    fun toDate(dateLong: Long?) = dateLong?.let { Date(it) }

    @TypeConverter
    fun fromDate(date: Date?) = date?.time
}