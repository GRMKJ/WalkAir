package com.cardomomo.walkair.presentation

import java.time.LocalDateTime
import java.time.LocalTime

data class WearData(
    val start: LocalDateTime,
    val ending: LocalDateTime,
    val heartRate: Float,
    val steps: Int,
    val calories: Float,
    val lastSync: LocalTime
)