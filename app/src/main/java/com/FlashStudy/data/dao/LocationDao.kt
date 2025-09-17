package com.FlashStudy.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.FlashStudy.data.model.Location
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations ORDER BY timestamp DESC LIMIT 7")
    fun getRecentLocations(): Flow<List<Location>>

    @Insert
    suspend fun insertLocation(location: Location): Long

    @Delete
    suspend fun deleteLocation(location: Location)

    @Query("DELETE FROM locations WHERE id NOT IN (SELECT id FROM locations ORDER BY timestamp DESC LIMIT 7)")
    suspend fun deleteOldestIfNeeded()
}