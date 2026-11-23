
package com.bhtravel.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.bhtravel.app.data.entity.FavoriteOfferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteOfferDao {

    @Query("SELECT * FROM favorite_offers ORDER BY destination ASC")
    fun getAllFavorites(): Flow<List<FavoriteOfferEntity>>

    @Query("SELECT * FROM favorite_offers WHERE destination = :destination LIMIT 1")
    suspend fun getFavorite(destination: String): FavoriteOfferEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteOfferEntity)

    @Delete
    suspend fun removeFavorite(favorite: FavoriteOfferEntity)

    @Query("DELETE FROM favorite_offers WHERE destination = :destination")
    suspend fun removeFavoriteByDestination(destination: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_offers WHERE destination = :destination)")
    suspend fun isFavorite(destination: String): Boolean
}
