package com.cardomomo.walkair

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WearDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: WearData)

    @Query("SELECT * FROM entrenamientos ORDER BY start DESC")
    fun getAll(): Flow<List<WearData>>

    @Query("DELETE FROM entrenamientos")
    suspend fun clearAll()

    @Delete
    suspend fun delete(data: WearData)
}
