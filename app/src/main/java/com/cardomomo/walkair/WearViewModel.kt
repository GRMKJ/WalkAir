package com.cardomomo.walkair

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class WearViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = Room.databaseBuilder(
        application,
        WearDatabase::class.java, "wear_database"
    ).build().wearDataDao()

    val allTrainings: Flow<List<WearData>> = dao.getAllTrainings().distinctUntilChanged()

    fun insertTraining(wearData: WearData) {
        viewModelScope.launch {
            dao.insert(wearData)
        }
    }
}