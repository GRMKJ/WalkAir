package com.cardomomo.walkair

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(tableName = "entrenamientos")
data class WearData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val start: LocalDateTime,
    val ending: LocalDateTime,
    val heartRate: Float,
    val steps: Int,
    val calories: Float,
    val lastSync: LocalTime
)