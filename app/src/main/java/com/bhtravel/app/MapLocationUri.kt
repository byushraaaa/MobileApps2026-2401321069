package com.bhtravel.app

/** Builds a Maps-compatible geo URI for a latitude/longitude pair. */
object MapLocationUri {
    fun forCoordinates(
        latitude: Double,
        longitude: Double,
    ): String {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90." }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180." }
        return "geo:$latitude,$longitude?q=$latitude,$longitude"
    }
}
