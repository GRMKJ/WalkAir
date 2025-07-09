package com.cardomomo.walkair

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [WearData::class], version = 1)
@TypeConverters(Converters::class)
abstract class WearDatabase : RoomDatabase() {
    abstract fun wearDataDao(): WearDataDao
}