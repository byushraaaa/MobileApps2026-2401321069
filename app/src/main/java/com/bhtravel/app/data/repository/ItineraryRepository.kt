
package com.bhtravel.app.data.repository

import com.bhtravel.app.data.dao.ItineraryDao
import com.bhtravel.app.data.entity.ItineraryItemEntity
import kotlinx.coroutines.flow.Flow

class ItineraryRepository(
    private val itineraryDao: ItineraryDao,
) {
    fun getItemsForTrip(tripId: Int): Flow<List<ItineraryItemEntity>> = itineraryDao.getItemsForTrip(tripId)

    suspend fun insertItem(item: ItineraryItemEntity) {
        itineraryDao.insertItem(item)
    }

    suspend fun updateItem(item: ItineraryItemEntity) {
        itineraryDao.updateItem(item)
    }

    suspend fun deleteItem(item: ItineraryItemEntity) {
        itineraryDao.deleteItem(item)
    }
}
