
package com.bhtravel.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bhtravel.app.data.entity.TripEntity
import com.bhtravel.app.data.repository.TripRepository
import kotlinx.coroutines.launch

class TripViewModel(
    private val repository: TripRepository,
) : ViewModel() {
    val allTrips: LiveData<List<TripEntity>> =
        repository.allTrips.asLiveData()

    fun insertTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.insertTrip(trip)
        }
    }

    fun updateTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.updateTrip(trip)
        }
    }

    fun deleteTrip(trip: TripEntity) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }
}
