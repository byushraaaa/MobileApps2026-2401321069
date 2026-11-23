
package com.bhtravel.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_offers")
data class FavoriteOfferEntity(
    @PrimaryKey
    val destination: String,
    val country: String,
    val description: String,
    val duration: String,
    val price: String,
    val imageName: String
)
