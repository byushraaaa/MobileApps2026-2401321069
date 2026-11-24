
package com.bhtravel.app.data.repository

import com.bhtravel.app.data.dao.ItineraryDao
import com.bhtravel.app.data.entity.ItineraryItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ItineraryRepositoryTest {
    private class FakeItineraryDao : ItineraryDao {
        private val items = MutableStateFlow<List<ItineraryItemEntity>>(emptyList())

        override fun getItemsForTrip(tripId: Int): Flow<List<ItineraryItemEntity>> =
            MutableStateFlow(items.value.filter { it.tripId == tripId })

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
        tripId: Int = 10,
        title: String = "Visit Eiffel Tower",
    ) = ItineraryItemEntity(
        id = id,
        tripId = tripId,
        title = title,
        description = "Sightseeing",
        date = "2026-12-10",
        time = "10:00",
        location = "Paris",
    )

    @Test
    fun insertItem_addsItem() =
        runBlocking {
            val repository = ItineraryRepository(FakeItineraryDao())
            repository.insertItem(createItem())

            assertEquals(1, repository.getItemsForTrip(10).first().size)
        }

    @Test
    fun getItemsForTrip_returnsOnlyMatchingItems() =
        runBlocking {
            val repository = ItineraryRepository(FakeItineraryDao())
            repository.insertItem(createItem())
            repository.insertItem(createItem(id = 2, tripId = 20))

            assertEquals(1, repository.getItemsForTrip(10).first().size)
        }

    @Test
    fun updateItem_changesExistingItem() =
        runBlocking {
            val repository = ItineraryRepository(FakeItineraryDao())
            repository.insertItem(createItem())

            repository.updateItem(createItem(title = "Visit Louvre"))

            assertEquals(
                "Visit Louvre",
                repository
                    .getItemsForTrip(10)
                    .first()
                    .first()
                    .title,
            )
        }

    @Test
    fun deleteItem_removesItem() =
        runBlocking {
            val repository = ItineraryRepository(FakeItineraryDao())
            val item = createItem()
            repository.insertItem(item)

            repository.deleteItem(item)

            assertTrue(repository.getItemsForTrip(10).first().isEmpty())
        }

    @Test
    fun getItemsForTrip_returnsEmptyListInitially() =
        runBlocking {
            val repository = ItineraryRepository(FakeItineraryDao())

            assertTrue(repository.getItemsForTrip(10).first().isEmpty())
        }
}
