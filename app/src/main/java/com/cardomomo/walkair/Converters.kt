package com.cardomomo.walkair

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): String = dateTime.format(dateTimeFormatter)

    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime = LocalDateTime.parse(value, dateTimeFormatter)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): String = time.format(timeFormatter)

    @TypeConverter
    fun toLocalTime(value: String): LocalTime = LocalTime.parse(value, timeFormatter)
}
