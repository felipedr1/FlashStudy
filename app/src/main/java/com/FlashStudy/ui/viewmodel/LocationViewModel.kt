package com.FlashStudy.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.FlashStudy.data.database.AppDatabase
import com.FlashStudy.data.model.Location
import com.FlashStudy.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LocationRepository
    private val _locations = MutableStateFlow<List<Location>>(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    private val _isDialogOpen = MutableStateFlow(false)
    val isDialogOpen: StateFlow<Boolean> = _isDialogOpen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LocationRepository(database.locationDao())
        viewModelScope.launch {
            repository.recentLocations.collect { locations ->
                _locations.value = locations
            }
        }
    }

    fun addLocation(name: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            repository.insertLocation(Location(
                name = name,
                latitude = latitude,
                longitude = longitude
            ))
        }
    }

    fun deleteLocation(location: Location) {
        viewModelScope.launch {
            repository.deleteLocation(location)
        }
    }

    fun showDialog() {
        _isDialogOpen.value = true
    }

    fun hideDialog() {
        _isDialogOpen.value = false
    }
}