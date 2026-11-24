
package com.bhtravel.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bhtravel.app.data.entity.ItineraryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItineraryDao {
    @Query(
        """
        SELECT * FROM itinerary_items
        WHERE tripId = :tripId
        ORDER BY date ASC, time ASC
    """,
    )
    fun getItemsForTrip(tripId: Int): Flow<List<ItineraryItemEntity>>

    @Insert
    suspend fun insertItem(item: ItineraryItemEntity)

    @Update
    suspend fun updateItem(item: ItineraryItemEntity)

    @Delete
    suspend fun deleteItem(item: ItineraryItemEntity)
}
