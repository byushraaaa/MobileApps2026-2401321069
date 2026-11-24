
package com.bhtravel.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.ItineraryItemEntity
import com.bhtravel.app.data.entity.TripEntity
import com.bhtravel.app.data.repository.ItineraryRepository
import com.bhtravel.app.data.repository.TripRepository
import com.bhtravel.app.viewmodel.ItineraryViewModel
import com.bhtravel.app.viewmodel.ItineraryViewModelFactory
import com.bhtravel.app.viewmodel.TripViewModel
import com.bhtravel.app.viewmodel.TripViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ItineraryActivity : AppCompatActivity() {
    private lateinit var tripViewModel: TripViewModel
    private lateinit var itineraryViewModel: ItineraryViewModel

    private lateinit var spinnerTrips: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etTime: EditText
    private lateinit var etLocation: EditText
    private lateinit var activitiesContainer: LinearLayout
    private lateinit var tvTripName: TextView
    private lateinit var tvActivityCount: TextView
    private lateinit var tvEmpty: TextView

    private var trips: List<TripEntity> = emptyList()
    private var selectedTrip: TripEntity? = null

    private var currentItemsLiveData:
        LiveData<List<ItineraryItemEntity>>? = null

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itinerary)

        initializeViews()
        initializeViewModels()
        setupDateAndTimePickers()
        setupButtons()
        observeTrips()
    }

    private fun initializeViews() {
        spinnerTrips = findViewById(R.id.spinnerItineraryTrips)
        etTitle = findViewById(R.id.etActivityTitle)
        etDescription = findViewById(R.id.etActivityDescription)
        etDate = findViewById(R.id.etActivityDate)
        etTime = findViewById(R.id.etActivityTime)
        etLocation = findViewById(R.id.etActivityLocation)

        activitiesContainer = findViewById(R.id.itineraryContainer)
        tvTripName = findViewById(R.id.tvItineraryTripName)
        tvActivityCount = findViewById(R.id.tvActivityCount)
        tvEmpty = findViewById(R.id.tvEmptyItinerary)
    }

    private fun initializeViewModels() {
        val database = DatabaseProvider.getDatabase(this)

        val tripRepository = TripRepository(database.tripDao())
        val tripFactory = TripViewModelFactory(tripRepository)

        tripViewModel =
            androidx.lifecycle.ViewModelProvider(
                this,
                tripFactory,
            )[TripViewModel::class.java]

        val itineraryRepository =
            ItineraryRepository(database.itineraryDao())

        val itineraryFactory =
            ItineraryViewModelFactory(itineraryRepository)

        itineraryViewModel =
            androidx.lifecycle.ViewModelProvider(
                this,
                itineraryFactory,
            )[ItineraryViewModel::class.java]
    }

    private fun setupDateAndTimePickers() {
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    etDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
            ).show()
        }

        etTime.setOnClickListener {
            val calendar = Calendar.getInstance()

            TimePickerDialog(
                this,
                { _, hour, minute ->
                    etTime.setText(
                        String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            hour,
                            minute,
                        ),
                    )
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            ).show()
        }
    }

    private fun setupButtons() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnAddActivity)
            .setOnClickListener {
                addActivity()
            }
    }

    private fun observeTrips() {
        tripViewModel.allTrips.observe(this) { tripList ->
            trips = tripList

            val tripNames =
                trips.map {
                    "${it.destination}, ${it.country}"
                }

            val adapter =
                ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    tripNames,
                )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item,
            )

            spinnerTrips.adapter = adapter

            if (trips.isEmpty()) {
                selectedTrip = null
                tvTripName.text = "No trips available"
                tvActivityCount.text = "0 planned activities"
                activitiesContainer.removeAllViews()
                tvEmpty.visibility = View.VISIBLE
                tvEmpty.text = "Create a trip first to plan activities."
            } else {
                spinnerTrips.onItemSelectedListener =
                    object : android.widget.AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: android.widget.AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long,
                        ) {
                            selectedTrip = trips.getOrNull(position)
                            selectedTrip?.let { trip ->
                                tvTripName.text =
                                    "${trip.destination}, ${trip.country}"
                                observeActivities(trip.id)
                            }
                        }

                        override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                            selectedTrip = null
                        }
                    }
            }
        }
    }

    private fun observeActivities(tripId: Int) {
        currentItemsLiveData?.removeObservers(this)

        currentItemsLiveData =
            itineraryViewModel.getItemsForTrip(tripId)

        currentItemsLiveData?.observe(this) { items ->
            displayActivities(items)
        }
    }

    private fun addActivity() {
        val trip = selectedTrip

        if (trip == null) {
            Toast
                .makeText(
                    this,
                    "Please select a trip first.",
                    Toast.LENGTH_SHORT,
                ).show()
            return
        }

        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val date = etDate.text.toString().trim()
        val time = etTime.text.toString().trim()
        val location = etLocation.text.toString().trim()

        if (
            title.isEmpty() ||
            date.isEmpty() ||
            time.isEmpty() ||
            location.isEmpty()
        ) {
            Toast
                .makeText(
                    this,
                    "Please complete the title, date, time and location.",
                    Toast.LENGTH_LONG,
                ).show()
            return
        }

        val item =
            ItineraryItemEntity(
                tripId = trip.id,
                title = title,
                description = description,
                date = date,
                time = time,
                location = location,
            )

        itineraryViewModel.insertItem(item)

        clearForm()

        Toast
            .makeText(
                this,
                "Activity added successfully!",
                Toast.LENGTH_SHORT,
            ).show()
    }

    private fun clearForm() {
        etTitle.text.clear()
        etDescription.text.clear()
        etDate.text.clear()
        etTime.text.clear()
        etLocation.text.clear()
    }

    private fun displayActivities(items: List<ItineraryItemEntity>) {
        activitiesContainer.removeAllViews()

        tvActivityCount.text =
            "${items.size} planned activities"

        tvEmpty.visibility =
            if (items.isEmpty()) View.VISIBLE else View.GONE

        items.forEach { item ->
            val card =
                MaterialCardView(this).apply {
                    radius = 18f
                    cardElevation = 2f
                    setCardBackgroundColor(
                        android.graphics.Color.WHITE,
                    )
                    useCompatPadding = true
                    layoutParams =
                        LinearLayout
                            .LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                            ).apply {
                                bottomMargin = 12
                            }
                }

            val content =
                LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(18, 16, 18, 16)
                }

            val title =
                TextView(this).apply {
                    text = item.title
                    textSize = 18f
                    setTextColor(
                        android.graphics.Color.rgb(23, 50, 77),
                    )
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }

            val details =
                TextView(this).apply {
                    text = "${item.date}  •  ${item.time}\n${item.location}"
                    textSize = 14f
                    setTextColor(
                        android.graphics.Color.rgb(100, 116, 130),
                    )
                    setPadding(0, 8, 0, 0)
                }

            content.addView(title)
            content.addView(details)

            if (item.description.isNotBlank()) {
                val description =
                    TextView(this).apply {
                        text = item.description
                        textSize = 14f
                        setTextColor(
                            android.graphics.Color.rgb(100, 116, 130),
                        )
                        setPadding(0, 8, 0, 0)
                    }
                content.addView(description)
            }

            val deleteButton =
                MaterialButton(
                    this,
                    null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle,
                ).apply {
                    text = "Delete"
                    isAllCaps = false
                    setOnClickListener {
                        confirmDelete(item)
                    }
                }

            content.addView(deleteButton)
            card.addView(content)
            activitiesContainer.addView(card)
        }
    }

    private fun confirmDelete(item: ItineraryItemEntity) {
        AlertDialog
            .Builder(this)
            .setTitle("Delete activity")
            .setMessage(
                "Are you sure you want to delete \"${item.title}\"?",
            ).setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                itineraryViewModel.deleteItem(item)

                Toast
                    .makeText(
                        this,
                        "Activity deleted.",
                        Toast.LENGTH_SHORT,
                    ).show()
            }.show()
    }
}
