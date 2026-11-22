
package com.bhtravel.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bhtravel.app.data.entity.ItineraryItemEntity
import com.bhtravel.app.data.repository.ItineraryRepository
import kotlinx.coroutines.launch

class ItineraryViewModel(
    private val repository: ItineraryRepository
) : ViewModel() {

    fun getItemsForTrip(tripId: Int): LiveData<List<ItineraryItemEntity>> {
        return repository.getItemsForTrip(tripId).asLiveData()
    }

    fun insertItem(item: ItineraryItemEntity) {
        viewModelScope.launch {
            repository.insertItem(item)
        }
    }

    fun updateItem(item: ItineraryItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item)
        }
    }

    fun deleteItem(item: ItineraryItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}
