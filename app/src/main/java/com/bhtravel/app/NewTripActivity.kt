
package com.bhtravel.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.TripEntity
import com.bhtravel.app.data.repository.TripRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NewTripActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TRIP_ID = "trip_id"
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_COUNTRY = "extra_country"
    }

    private lateinit var etDestination: EditText
    private lateinit var etCountry: EditText
    private lateinit var etTravelers: EditText
    private lateinit var etNotes: EditText

    private lateinit var btnStartDate: Button
    private lateinit var btnEndDate: Button
    private lateinit var btnSaveTrip: Button

    private lateinit var repository: TripRepository

    private var startDate: Calendar? = null
    private var endDate: Calendar? = null
    private var editingTripId: Int = 0

    private var originalImageName: String? = null
    private var originalDestination: String? = null

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_trip)

        etDestination = findViewById(R.id.etDestination)
        etCountry = findViewById(R.id.etCountry)
        etTravelers = findViewById(R.id.etTravelers)
        etNotes = findViewById(R.id.etNotes)

        btnStartDate = findViewById(R.id.btnStartDate)
        btnEndDate = findViewById(R.id.btnEndDate)
        btnSaveTrip = findViewById(R.id.btnSaveTrip)

        val btnCancelTrip = findViewById<Button>(R.id.btnCancelTrip)

        val database = DatabaseProvider.getDatabase(applicationContext)
        repository = TripRepository(database.tripDao())

        editingTripId = intent.getIntExtra(EXTRA_TRIP_ID, 0)

        if (editingTripId != 0) {
            btnSaveTrip.text = "Update Trip"
            loadTrip(editingTripId)
        } else {
            etDestination.setText(
                intent.getStringExtra(EXTRA_DESTINATION) ?: "",
            )
            etCountry.setText(
                intent.getStringExtra(EXTRA_COUNTRY) ?: "",
            )
        }

        btnStartDate.setOnClickListener {
            showDatePicker(isStartDate = true)
        }

        btnEndDate.setOnClickListener {
            showDatePicker(isStartDate = false)
        }

        btnSaveTrip.setOnClickListener {
            validateTrip()
        }

        btnCancelTrip.setOnClickListener {
            finish()
        }
    }

    private fun loadTrip(tripId: Int) {
        lifecycleScope.launch {
            try {
                val trip = repository.getTripById(tripId)

                if (trip == null) {
                    Toast
                        .makeText(
                            this@NewTripActivity,
                            "Trip not found.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    finish()
                    return@launch
                }

                etDestination.setText(trip.destination)
                etCountry.setText(trip.country)
                etTravelers.setText(trip.travelers.toString())
                etNotes.setText(trip.notes)

                originalDestination = trip.destination
                originalImageName = trip.imageUrl

                startDate =
                    Calendar.getInstance().apply {
                        time = dateFormat.parse(trip.startDate)!!
                    }

                endDate =
                    Calendar.getInstance().apply {
                        time = dateFormat.parse(trip.endDate)!!
                    }

                btnStartDate.text = trip.startDate
                btnEndDate.text = trip.endDate
            } catch (exception: Exception) {
                Toast
                    .makeText(
                        this@NewTripActivity,
                        "Could not load the trip.",
                        Toast.LENGTH_LONG,
                    ).show()
                finish()
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val selectedDate = if (isStartDate) startDate else endDate
        val calendar =
            (selectedDate ?: Calendar.getInstance()).clone() as Calendar

        val datePicker =
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->

                    val chosenDate =
                        Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                    if (isStartDate) {
                        startDate = chosenDate

                        btnStartDate.text =
                            dateFormat.format(chosenDate.time)

                        if (endDate != null &&
                            endDate!!.before(chosenDate)
                        ) {
                            endDate = null
                            btnEndDate.text = "Choose end date"
                        }
                    } else {
                        val chosenStartDate = startDate

                        if (chosenStartDate != null &&
                            chosenDate.before(chosenStartDate)
                        ) {
                            Toast
                                .makeText(
                                    this,
                                    "End date cannot be before start date.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } else {
                            endDate = chosenDate

                            btnEndDate.text =
                                dateFormat.format(chosenDate.time)
                        }
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            )

        if (!isStartDate && startDate != null) {
            datePicker.datePicker.minDate =
                startDate!!.timeInMillis
        }

        datePicker.show()
    }

    private fun getDestinationImage(destination: String): String? =
        when (destination.trim().lowercase(Locale.ROOT)) {
            "plovdiv", "пловдив" -> "plovdiv"

            "dubai", "дубай" -> "dubai"

            "tokyo", "токио" -> "tokyo"

            "london", "лондон" -> "london"

            "milan", "milano", "милано" -> "milano"

            "vienna", "виена" -> "vienna"

            "amsterdam", "амстердам" -> "amsterdam"

            "paris", "париж" -> "paris"

            "rome", "roma", "рим" -> "rome"

            "budapest", "будапеща" -> "budapest"

            "santorini", "санторини" -> "santorini"

            "barcelona", "барселона" -> "barcelona"

            "cappadocia", "кападокия" -> "cappadocia"

            "sofia", "софия" -> "sofia"

            "nessbar", "nessebar", "несебър" -> "nessbar"

            else -> null
        }

    private fun validateTrip() {
        val destination = etDestination.text.toString().trim()
        val country = etCountry.text.toString().trim()
        val travelersText = etTravelers.text.toString().trim()
        val notes = etNotes.text.toString().trim()

        val validationError =
            TripValidator.validate(
                destination = destination,
                country = country,
                startDateMillis = startDate?.timeInMillis,
                endDateMillis = endDate?.timeInMillis,
                travelersText = travelersText,
            )

        when (validationError) {
            TripValidationError.DESTINATION_REQUIRED -> {
                etDestination.error = "Please enter a destination"
                etDestination.requestFocus()
                return
            }

            TripValidationError.COUNTRY_REQUIRED -> {
                etCountry.error = "Please enter a country"
                etCountry.requestFocus()
                return
            }

            TripValidationError.START_DATE_REQUIRED -> {
                Toast
                    .makeText(
                        this,
                        "Please choose a start date.",
                        Toast.LENGTH_SHORT,
                    ).show()
                return
            }

            TripValidationError.END_DATE_REQUIRED -> {
                Toast
                    .makeText(
                        this,
                        "Please choose an end date.",
                        Toast.LENGTH_SHORT,
                    ).show()
                return
            }

            TripValidationError.INVALID_DATE_RANGE -> {
                Toast
                    .makeText(
                        this,
                        "End date cannot be before start date.",
                        Toast.LENGTH_SHORT,
                    ).show()
                return
            }

            TripValidationError.INVALID_TRAVELERS -> {
                etTravelers.error = "Enter at least 1 traveler"
                etTravelers.requestFocus()
                return
            }

            null -> {
                // All fields are valid.
            }
        }

        val travelers = travelersText.toIntOrNull()!!

        val selectedImage =
            if (
                editingTripId != 0 &&
                destination.equals(originalDestination, ignoreCase = true)
            ) {
                originalImageName
            } else {
                getDestinationImage(destination)
            }

        val trip =
            TripEntity(
                id = editingTripId,
                destination = destination,
                country = country,
                startDate = dateFormat.format(startDate!!.time),
                endDate = dateFormat.format(endDate!!.time),
                travelers = travelers,
                notes = notes,
                imageUrl = selectedImage,
            )

        saveTrip(trip)
    }

    private fun saveTrip(trip: TripEntity) {
        lifecycleScope.launch {
            try {
                if (editingTripId == 0) {
                    repository.insertTrip(trip)

                    Toast
                        .makeText(
                            this@NewTripActivity,
                            "Trip saved successfully!",
                            Toast.LENGTH_LONG,
                        ).show()
                } else {
                    repository.updateTrip(trip)

                    Toast
                        .makeText(
                            this@NewTripActivity,
                            "Trip updated successfully!",
                            Toast.LENGTH_LONG,
                        ).show()
                }

                finish()
            } catch (exception: Exception) {
                Toast
                    .makeText(
                        this@NewTripActivity,
                        "Could not save the trip. Please try again.",
                        Toast.LENGTH_LONG,
                    ).show()
            }
        }
    }
}
