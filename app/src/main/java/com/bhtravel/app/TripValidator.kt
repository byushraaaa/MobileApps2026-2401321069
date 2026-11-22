
package com.bhtravel.app

enum class TripValidationError {
    DESTINATION_REQUIRED,
    COUNTRY_REQUIRED,
    START_DATE_REQUIRED,
    END_DATE_REQUIRED,
    INVALID_DATE_RANGE,
    INVALID_TRAVELERS
}

object TripValidator {

    fun validate(
        destination: String,
        country: String,
        startDateMillis: Long?,
        endDateMillis: Long?,
        travelersText: String
    ): TripValidationError? {

        if (destination.isBlank()) {
            return TripValidationError.DESTINATION_REQUIRED
        }

        if (country.isBlank()) {
            return TripValidationError.COUNTRY_REQUIRED
        }

        if (startDateMillis == null) {
            return TripValidationError.START_DATE_REQUIRED
        }

        if (endDateMillis == null) {
            return TripValidationError.END_DATE_REQUIRED
        }

        if (endDateMillis < startDateMillis) {
            return TripValidationError.INVALID_DATE_RANGE
        }

        val travelers = travelersText.toIntOrNull()

        if (travelers == null || travelers < 1) {
            return TripValidationError.INVALID_TRAVELERS
        }

        return null
    }
}
