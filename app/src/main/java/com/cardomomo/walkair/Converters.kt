package com.cardomomo.walkair

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    @RequiresApi(Build.VERSION_CODES.O)
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeFormatter)

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value, dateTimeFormatter)

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.format(timeFormatter)

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value, timeFormatter)
}
