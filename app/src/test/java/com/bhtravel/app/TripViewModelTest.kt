
package com.bhtravel.app.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.bhtravel.app.MainDispatcherRule
import com.bhtravel.app.data.dao.TripDao
import com.bhtravel.app.data.entity.TripEntity
import com.bhtravel.app.data.repository.TripRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private class FakeTripDao : TripDao {
        val trips = MutableStateFlow<List<TripEntity>>(emptyList())

        override fun getAllTrips(): Flow<List<TripEntity>> = trips

        override suspend fun getTripById(tripId: Int): TripEntity? = trips.value.find { it.id == tripId }

        override suspend fun insertTrip(trip: TripEntity) {
            trips.value = trips.value + trip
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

    private fun createTrip(
        id: Int = 1,
        destination: String = "Paris",
    ) = TripEntity(
        id = id,
        destination = destination,
        country = "France",
        startDate = "2026-12-10",
        endDate = "2026-12-15",
        travelers = 2,
        notes = "Test trip",
    )

    @Test
    fun allTrips_emitsTripsFromRepository() =
        runTest {
            val dao = FakeTripDao()
            dao.trips.value = listOf(createTrip())

            val viewModel = TripViewModel(TripRepository(dao))
            val observer = Observer<List<TripEntity>> {}

            viewModel.allTrips.observeForever(observer)
            advanceUntilIdle()

            assertEquals(1, viewModel.allTrips.value?.size)
            assertEquals(
                "Paris",
                viewModel.allTrips.value
                    ?.first()
                    ?.destination,
            )

            viewModel.allTrips.removeObserver(observer)
        }

    @Test
    fun insertTrip_addsTrip() =
        runTest {
            val dao = FakeTripDao()
            val viewModel = TripViewModel(TripRepository(dao))
            val observer = Observer<List<TripEntity>> {}

            viewModel.allTrips.observeForever(observer)
            viewModel.insertTrip(createTrip())
            advanceUntilIdle()

            assertEquals(1, viewModel.allTrips.value?.size)

            viewModel.allTrips.removeObserver(observer)
        }

    @Test
    fun updateTrip_changesTrip() =
        runTest {
            val dao = FakeTripDao()
            dao.trips.value = listOf(createTrip())

            val viewModel = TripViewModel(TripRepository(dao))
            val observer = Observer<List<TripEntity>> {}

            viewModel.allTrips.observeForever(observer)
            viewModel.updateTrip(createTrip(destination = "Rome"))
            advanceUntilIdle()

            assertEquals(
                "Rome",
                viewModel.allTrips.value
                    ?.first()
                    ?.destination,
            )

            viewModel.allTrips.removeObserver(observer)
        }

    @Test
    fun deleteTrip_removesTrip() =
        runTest {
            val dao = FakeTripDao()
            dao.trips.value = listOf(createTrip())

            val viewModel = TripViewModel(TripRepository(dao))
            val observer = Observer<List<TripEntity>> {}

            viewModel.allTrips.observeForever(observer)
            viewModel.deleteTrip(createTrip())
            advanceUntilIdle()

            assertTrue(viewModel.allTrips.value.isNullOrEmpty())

            viewModel.allTrips.removeObserver(observer)
        }
}
