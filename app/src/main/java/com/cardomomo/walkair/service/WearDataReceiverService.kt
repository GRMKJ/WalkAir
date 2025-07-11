package com.cardomomo.walkair.service

import android.util.Log
import com.cardomomo.walkair.WearData
import com.cardomomo.walkair.WearDatabase
import com.google.android.gms.wearable.*
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.LocalTime

class WearDataReceiverService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                if (item.uri.path == "/training_data") {
                    val map = DataMapItem.fromDataItem(item).dataMap
                    val start = LocalDateTime.parse(map.getString("start"))
                    val ending = LocalDateTime.parse(map.getString("ending"))
                    val steps = map.getInt("steps")
                    val calories = map.getFloat("calories")
                    val heartRate = map.getFloat("heartRate")
                    val lastSync = LocalTime.parse(map.getString("lastSync"))

                    Log.d("WearDataReceiver", "Datos recibidos: steps=$steps, calories=$calories, HR=$heartRate")

                    val wearData = WearData(
                        start = start,
                        ending = ending,
                        steps = steps,
                        calories = calories,
                        heartRate = heartRate,
                        lastSync = lastSync
                    )

                    val db = WearDatabase.getDatabase(applicationContext)
                    val dao = db.wearDataDao()

                    CoroutineScope(Dispatchers.IO).launch {
                        dao.insert(wearData)
                        Log.d("WearReceiver", "✔️ Datos guardados: $wearData")
                    }

                }
            }
        }
    }
}