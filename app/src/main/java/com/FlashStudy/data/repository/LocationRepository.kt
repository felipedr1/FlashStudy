package com.FlashStudy.data.repository

import com.FlashStudy.data.dao.LocationDao
import com.FlashStudy.data.model.Location
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationDao: LocationDao) {

    val recentLocations: Flow<List<Location>> = locationDao.getRecentLocations()

    suspend fun insertLocation(location: Location): Long {
        val id = locationDao.insertLocation(location)
        locationDao.deleteOldestIfNeeded()
        return id
    }

    suspend fun deleteLocation(location: Location) {
        locationDao.deleteLocation(location)
    }
}