
package com.bhtravel.app.data.repository

import com.bhtravel.app.data.dao.TripDao
import com.bhtravel.app.data.entity.TripEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class TripRepositoryTest {
    private class FakeTripDao : TripDao {
        private val trips = MutableStateFlow<List<TripEntity>>(emptyList())
        private var nextId = 1

        override fun getAllTrips(): Flow<List<TripEntity>> = trips

        override suspend fun getTripById(tripId: Int): TripEntity? = trips.value.find { it.id == tripId }

        override suspend fun insertTrip(trip: TripEntity) {
            val newTrip =
                if (trip.id == 0) {
                    trip.copy(id = nextId++)
                } else {
                    trip
                }

            trips.value = trips.value + newTrip
        }

        override suspend fun updateTrip(trip: TripEntity) {
            trips.value =
                trips.value.map {
                    if (it.id == trip.id) trip else it
                }
        }

        override suspend fun deleteTrip(trip: TripEntity) {
            deleteExpensesForTrip(trip.id)
            deleteItineraryItemsForTrip(trip.id)
            trips.value = trips.value.filterNot { it.id == trip.id }
        }

        override suspend fun deleteExpensesForTrip(tripId: Int) = Unit

        override suspend fun deleteItineraryItemsForTrip(tripId: Int) = Unit

        override suspend fun deleteTripById(tripId: Int) {
            trips.value = trips.value.filterNot { it.id == tripId }
        }
    }

    private fun createRepository(): TripRepository = TripRepository(FakeTripDao())

    private fun createTrip(
        id: Int = 0,
        destination: String = "Paris",
        country: String = "France",
    ): TripEntity =
        TripEntity(
            id = id,
            destination = destination,
            country = country,
            startDate = "2026-12-10",
            endDate = "2026-12-15",
            travelers = 2,
            notes = "Test trip",
        )

    @Test
    fun insertTrip_addsTripToRepository() =
        runBlocking {
            val repository = createRepository()

            repository.insertTrip(createTrip())

            val trips = repository.allTrips.first()

            assertEquals(1, trips.size)
            assertEquals("Paris", trips.first().destination)
        }

    @Test
    fun getTripById_returnsExistingTrip() =
        runBlocking {
            val repository = createRepository()

            repository.insertTrip(createTrip())

            val insertedTrip = repository.allTrips.first().first()
            val result = repository.getTripById(insertedTrip.id)

            assertNotNull(result)
            assertEquals("Paris", result?.destination)
        }

    @Test
    fun getTripById_returnsNullForMissingTrip() =
        runBlocking {
            val repository = createRepository()

            val result = repository.getTripById(999)

            assertNull(result)
        }

    @Test
    fun updateTrip_changesExistingTrip() =
        runBlocking {
            val repository = createRepository()

            repository.insertTrip(createTrip())

            val insertedTrip = repository.allTrips.first().first()
            val updatedTrip = insertedTrip.copy(destination = "Rome")

            repository.updateTrip(updatedTrip)

            val result = repository.getTripById(insertedTrip.id)

            assertEquals("Rome", result?.destination)
        }

    @Test
    fun deleteTrip_removesTripFromRepository() =
        runBlocking {
            val repository = createRepository()

            repository.insertTrip(createTrip())

            val insertedTrip = repository.allTrips.first().first()

            repository.deleteTrip(insertedTrip)

            assertEquals(0, repository.allTrips.first().size)
        }

    @Test
    fun allTrips_returnsEmptyListInitially() =
        runBlocking {
            val repository = createRepository()

            assertEquals(0, repository.allTrips.first().size)
        }
}
