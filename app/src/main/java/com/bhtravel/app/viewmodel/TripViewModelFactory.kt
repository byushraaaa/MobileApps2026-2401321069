
package com.bhtravel.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bhtravel.app.data.repository.TripRepository

class TripViewModelFactory(
    private val repository: TripRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            return TripViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
