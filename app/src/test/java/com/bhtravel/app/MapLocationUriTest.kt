package com.bhtravel.app

import org.junit.Assert.assertEquals
import org.junit.Test

class MapLocationUriTest {
    @Test
    fun createsMapsUriFromCoordinates() {
        assertEquals("geo:42.6977,23.3219?q=42.6977,23.3219", MapLocationUri.forCoordinates(42.6977, 23.3219))
    }

    @Test
    fun acceptsCoordinateBoundaries() {
        assertEquals("geo:-90.0,-180.0?q=-90.0,-180.0", MapLocationUri.forCoordinates(-90.0, -180.0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsLatitudeOutsideEarth() {
        MapLocationUri.forCoordinates(90.1, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsLongitudeOutsideEarth() {
        MapLocationUri.forCoordinates(0.0, 180.1)
    }
}
