
package com.bhtravel.app.data.repository

import com.bhtravel.app.data.dao.TripDao
import com.bhtravel.app.data.entity.TripEntity
import kotlinx.coroutines.flow.Flow

class TripRepository(
    private val tripDao: TripDao,
) {
    val allTrips: Flow<List<TripEntity>> =
        tripDao.getAllTrips()

    suspend fun getTripById(tripId: Int): TripEntity? = tripDao.getTripById(tripId)

    suspend fun insertTrip(trip: TripEntity) {
        tripDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: TripEntity) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: TripEntity) {
        tripDao.deleteTrip(trip)
    }
}
