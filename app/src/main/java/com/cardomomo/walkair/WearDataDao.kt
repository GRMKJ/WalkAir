package com.cardomomo.walkair

import androidx.room.*

@Dao
interface WearDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: WearData)

    @Query("SELECT * FROM entrenamientos ORDER BY start DESC")
    suspend fun getAll(): List<WearData>

    @Delete
    suspend fun delete(data: WearData)
}