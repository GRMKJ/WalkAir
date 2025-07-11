package com.cardomomo.walkair

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [WearData::class], version = 1)
@TypeConverters(Converters::class)
abstract class WearDatabase : RoomDatabase() {
    abstract fun wearDataDao(): WearDataDao

    companion object {
        @Volatile private var INSTANCE: WearDatabase? = null

        fun getDatabase(context: Context): WearDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WearDatabase::class.java,
                    "wear_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}