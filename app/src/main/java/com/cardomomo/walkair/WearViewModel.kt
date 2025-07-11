package com.cardomomo.walkair

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class WearViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = WearDatabase.getDatabase(application).wearDataDao()

    val getAll: Flow<List<WearData>> = dao.getAll().distinctUntilChanged()

    fun insertTraining(wearData: WearData) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(wearData)
        }
    }
}