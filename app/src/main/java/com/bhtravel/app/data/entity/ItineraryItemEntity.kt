package com.bhtravel.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "itinerary_items")
data class ItineraryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int,
    val title: String,
    val description: String,
    val date: String,
    val time: String,
    val location: String,
)
