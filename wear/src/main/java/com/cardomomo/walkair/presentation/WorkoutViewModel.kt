package com.cardomomo.walkair.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class WorkoutViewModel : ViewModel() {
    var steps by mutableStateOf(0)
        private set

    var heartRate by mutableStateOf(0f)
        private set

    var totalStepsAtStart = 0f
        private set

    fun updateSteps(raw: Float) {
        if (totalStepsAtStart == 0f) {
            totalStepsAtStart = raw
        }
        steps = (raw - totalStepsAtStart).toInt()
    }

    fun resetSteps() {
        totalStepsAtStart = 0f
        steps = 0
    }

    fun updateHeartRate(value: Float) {
        heartRate = value
    }
}