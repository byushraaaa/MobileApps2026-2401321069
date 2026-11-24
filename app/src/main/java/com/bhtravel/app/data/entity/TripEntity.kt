
package com.bhtravel.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val destination: String,
    val country: String,
    val startDate: String,
    val endDate: String,
    val travelers: Int,
    val notes: String,
    val imageUrl: String? = null,
)
