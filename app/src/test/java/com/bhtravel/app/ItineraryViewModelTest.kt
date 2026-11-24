
package com.bhtravel.app.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.bhtravel.app.MainDispatcherRule
import com.bhtravel.app.data.dao.ItineraryDao
import com.bhtravel.app.data.entity.ItineraryItemEntity
import com.bhtravel.app.data.repository.ItineraryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItineraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private class FakeItineraryDao : ItineraryDao {
        val items = MutableStateFlow<List<ItineraryItemEntity>>(emptyList())

        override fun getItemsForTrip(tripId: Int): Flow<List<ItineraryItemEntity>> =
            items.map { list -> list.filter { it.tripId == tripId } }

        override suspend fun insertItem(item: ItineraryItemEntity) {
            items.value = items.value + item
        }

        override suspend fun updateItem(item: ItineraryItemEntity) {
            items.value =
                items.value.map {
                    if (it.id == item.id) item else it
                }
        }

        override suspend fun deleteItem(item: ItineraryItemEntity) {
            items.value = items.value.filterNot { it.id == item.id }
        }
    }

    private fun createItem(
        id: Int = 1,
        title: String = "Eiffel Tower",
    ) = ItineraryItemEntity(
        id = id,
        tripId = 10,
        title = title,
        description = "Sightseeing",
        date = "2026-12-10",
        time = "10:00",
        location = "Paris",
    )

    @Test
    fun getItemsForTrip_returnsItems() =
        runTest {
            val dao = FakeItineraryDao()
            dao.items.value = listOf(createItem())

            val viewModel = ItineraryViewModel(ItineraryRepository(dao))
            val liveData = viewModel.getItemsForTrip(10)
            val observer = Observer<List<ItineraryItemEntity>> {}

            liveData.observeForever(observer)
            advanceUntilIdle()

            assertEquals(1, liveData.value?.size)

            liveData.removeObserver(observer)
        }

    @Test
    fun insertItem_addsItem() =
        runTest {
            val dao = FakeItineraryDao()
            val viewModel = ItineraryViewModel(ItineraryRepository(dao))

            viewModel.insertItem(createItem())
            advanceUntilIdle()

            assertEquals(1, dao.items.value.size)
        }

    @Test
    fun updateItem_changesItem() =
        runTest {
            val dao = FakeItineraryDao()
            dao.items.value = listOf(createItem())

            val viewModel = ItineraryViewModel(ItineraryRepository(dao))
            viewModel.updateItem(createItem(title = "Louvre Museum"))
            advanceUntilIdle()

            assertEquals(
                "Louvre Museum",
                dao.items.value
                    .first()
                    .title,
            )
        }

    @Test
    fun deleteItem_removesItem() =
        runTest {
            val dao = FakeItineraryDao()
            dao.items.value = listOf(createItem())

            val viewModel = ItineraryViewModel(ItineraryRepository(dao))
            viewModel.deleteItem(createItem())
            advanceUntilIdle()

            assertTrue(dao.items.value.isEmpty())
        }
}
