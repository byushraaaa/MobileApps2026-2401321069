
package com.bhtravel.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TripValidatorTest {
    private val validStartDate = 1000L
    private val validEndDate = 2000L

    @Test
    fun validTrip_returnsNoError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "2",
            )

        assertNull(result)
    }

    @Test
    fun emptyDestination_returnsDestinationError() {
        val result =
            TripValidator.validate(
                "",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "2",
            )

        assertEquals(
            TripValidationError.DESTINATION_REQUIRED,
            result,
        )
    }

    @Test
    fun emptyCountry_returnsCountryError() {
        val result =
            TripValidator.validate(
                "Sofia",
                " ",
                validStartDate,
                validEndDate,
                "2",
            )

        assertEquals(
            TripValidationError.COUNTRY_REQUIRED,
            result,
        )
    }

    @Test
    fun missingStartDate_returnsStartDateError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                null,
                validEndDate,
                "2",
            )

        assertEquals(
            TripValidationError.START_DATE_REQUIRED,
            result,
        )
    }

    @Test
    fun missingEndDate_returnsEndDateError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                null,
                "2",
            )

        assertEquals(
            TripValidationError.END_DATE_REQUIRED,
            result,
        )
    }

    @Test
    fun endDateBeforeStartDate_returnsDateRangeError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                2000L,
                1000L,
                "2",
            )

        assertEquals(
            TripValidationError.INVALID_DATE_RANGE,
            result,
        )
    }

    @Test
    fun zeroTravelers_returnsTravelersError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "0",
            )

        assertEquals(
            TripValidationError.INVALID_TRAVELERS,
            result,
        )
    }

    @Test
    fun invalidTravelersText_returnsTravelersError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "abc",
            )

        assertEquals(
            TripValidationError.INVALID_TRAVELERS,
            result,
        )
    }

    @Test
    fun whitespaceDestination_returnsDestinationError() {
        val result =
            TripValidator.validate(
                "   ",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "2",
            )

        assertEquals(
            TripValidationError.DESTINATION_REQUIRED,
            result,
        )
    }

    @Test
    fun whitespaceCountry_returnsCountryError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "   ",
                validStartDate,
                validEndDate,
                "2",
            )

        assertEquals(
            TripValidationError.COUNTRY_REQUIRED,
            result,
        )
    }

    @Test
    fun negativeTravelers_returnsTravelersError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "-1",
            )

        assertEquals(
            TripValidationError.INVALID_TRAVELERS,
            result,
        )
    }

    @Test
    fun decimalTravelers_returnsTravelersError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "2.5",
            )

        assertEquals(
            TripValidationError.INVALID_TRAVELERS,
            result,
        )
    }

    @Test
    fun sameStartAndEndDate_returnsNoError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                1000L,
                1000L,
                "2",
            )

        assertNull(result)
    }

    @Test
    fun emptyTravelers_returnsTravelersError() {
        val result =
            TripValidator.validate(
                "Sofia",
                "Bulgaria",
                validStartDate,
                validEndDate,
                "",
            )

        assertEquals(
            TripValidationError.INVALID_TRAVELERS,
            result,
        )
    }
}
