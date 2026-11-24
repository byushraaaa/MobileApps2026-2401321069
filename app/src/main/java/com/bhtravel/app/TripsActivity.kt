
package com.bhtravel.app

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.TripEntity
import com.bhtravel.app.data.repository.TripRepository
import com.bhtravel.app.viewmodel.TripViewModel
import com.bhtravel.app.viewmodel.TripViewModelFactory
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripsActivity : AppCompatActivity() {

    private lateinit var tripsContainer: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var tripViewModel: TripViewModel
    private lateinit var repository: TripRepository

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    // Colors automatically adapt to Light and Dark Mode
    private val appBackground: Int
        get() = getColor(R.color.app_background)

    private val appCard: Int
        get() = getColor(R.color.app_card)

    private val appText: Int
        get() = getColor(R.color.app_text)

    private val appSecondaryText: Int
        get() = getColor(R.color.app_secondary_text)

    private val appDivider: Int
        get() = getColor(R.color.app_divider)

    private val appSelected: Int
        get() = getColor(R.color.app_selected)

    private val appUnselected: Int
        get() = getColor(R.color.app_unselected)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)

        tripsContainer = findViewById(R.id.tripsContainer)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        findViewById<View>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val database = DatabaseProvider.getDatabase(this)
        repository = TripRepository(database.tripDao())

        val factory = TripViewModelFactory(repository)
        tripViewModel =
            ViewModelProvider(this, factory)[TripViewModel::class.java]

        tripViewModel.allTrips.observe(this) { trips ->
            displayTrips(trips)
        }

        lifecycleScope.launch {
            addDemoTripsIfMissing()
        }
    }

    private suspend fun addDemoTripsIfMissing() {
        val existingTrips = repository.allTrips.first()

        val existingDestinations = existingTrips.map {
            it.destination.lowercase(Locale.ROOT)
        }.toSet()

        val demoTrips = listOf(
            TripEntity(
                destination = "Milan",
                country = "Italy",
                startDate = "06 Dec 2026",
                endDate = "09 Dec 2026",
                travelers = 2,
                notes = "",
                imageUrl = "milano"
            ),
            TripEntity(
                destination = "Vienna",
                country = "Austria",
                startDate = "31 Dec 2026",
                endDate = "02 Jan 2027",
                travelers = 2,
                notes = "",
                imageUrl = "vienna"
            ),
            TripEntity(
                destination = "Amsterdam",
                country = "Netherlands",
                startDate = "12 Feb 2027",
                endDate = "15 Feb 2027",
                travelers = 2,
                notes = "",
                imageUrl = "amsterdam"
            ),
            TripEntity(
                destination = "Istanbul",
                country = "Turkey",
                startDate = "15 Aug 2026",
                endDate = "18 Aug 2026",
                travelers = 2,
                notes = "",
                imageUrl = "istanbul"
            ),
            TripEntity(
                destination = "Tromsø",
                country = "Norway",
                startDate = "10 Jul 2026",
                endDate = "12 Jul 2026",
                travelers = 2,
                notes = "",
                imageUrl = "tromso"
            ),
            TripEntity(
                destination = "Sofia",
                country = "Bulgaria",
                startDate = "20 May 2026",
                endDate = "22 May 2026",
                travelers = 2,
                notes = "",
                imageUrl = "sofia"
            )
        )

        demoTrips.forEach { trip ->
            if (
                trip.destination.lowercase(Locale.ROOT)
                !in existingDestinations
            ) {
                repository.insertTrip(trip)
            }
        }
    }

    private fun displayTrips(trips: List<TripEntity>) {
        tripsContainer.removeAllViews()

        if (trips.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            return
        }

        tvEmptyState.visibility = View.GONE

        val today = Date()

        val upcomingTrips = trips
            .filter {
                parseDate(it.startDate)?.let { date ->
                    !date.before(today)
                } ?: false
            }
            .sortedBy { parseDate(it.startDate) }

        val pastTrips = trips
            .filter {
                parseDate(it.startDate)?.let { date ->
                    date.before(today)
                } ?: true
            }
            .sortedByDescending { parseDate(it.startDate) }

        if (upcomingTrips.isNotEmpty()) {
            addSectionTitle("Upcoming Trips (${upcomingTrips.size})")

            upcomingTrips.forEach { trip ->
                tripsContainer.addView(createTripCard(trip))
            }
        }

        if (pastTrips.isNotEmpty()) {
            addSectionTitle("Past Trips (${pastTrips.size})")

            pastTrips.forEach { trip ->
                tripsContainer.addView(createTripCard(trip))
            }
        }
    }

    // Section titles now use the active theme text color
    private fun addSectionTitle(title: String) {
        val textView = TextView(this).apply {
            text = title
            textSize = 21f
            setTextColor(appText)
            setPadding(dp(4), dp(22), dp(4), dp(10))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        tripsContainer.addView(textView)
    }

    private fun createTripCard(trip: TripEntity): View {

        val card = MaterialCardView(this).apply {
            radius = dp(22).toFloat()
            cardElevation = dp(4).toFloat()
            strokeWidth = dp(1)
            strokeColor = appDivider
            setCardBackgroundColor(appCard)
            useCompatPadding = true

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, dp(7), 0, dp(15))
            }
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
        }

        // Destination image
        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(175)
            )

            scaleType = ImageView.ScaleType.CENTER_CROP

            val imageName = trip.imageUrl
                ?.takeIf { it.isNotBlank() }
                ?: getDestinationImage(trip.destination)

            val imageId = resources.getIdentifier(
                imageName,
                "drawable",
                packageName
            )

            setImageResource(
                if (imageId != 0) imageId else R.drawable.travel
            )
        }

        mainLayout.addView(image)

        // Destination title
        val destination = TextView(this).apply {
            text = "${trip.destination}, ${trip.country}"
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(appText)
            setPadding(0, dp(13), 0, dp(6))
        }

        mainLayout.addView(destination)

        // Dates
        mainLayout.addView(
            createDetailText(
                "📅  ${trip.startDate} – ${trip.endDate}"
            )
        )

        // Travelers
        mainLayout.addView(
            createDetailText(
                "👥  ${trip.travelers} travelers"
            )
        )

        // Notes
        if (trip.notes.isNotBlank()) {
            mainLayout.addView(
                createDetailText("📝  ${trip.notes}")
            )
        }

        // Divider
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(12)
                bottomMargin = dp(12)
            }

            setBackgroundColor(appDivider)
        }

        mainLayout.addView(divider)

        // Action buttons
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val editButton = createActionButton(
            "✎  Edit",
            appSelected,
            appText
        ) {
            openEditTrip(trip)
        }

        val deleteButton = createActionButton(
            "▤  Delete",
            appUnselected,
            appText
        ) {
            showDeleteConfirmation(trip)
        }

        val mapsButton = createActionButton(
            "⌖  Maps",
            appSelected,
            appText
        ) {
            openInMaps(trip)
        }

        buttons.addView(
            editButton,
            LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                marginEnd = dp(5)
            }
        )

        buttons.addView(
            deleteButton,
            LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                marginEnd = dp(5)
            }
        )

        buttons.addView(
            mapsButton,
            LinearLayout.LayoutParams(0, dp(46), 1f)
        )

        mainLayout.addView(buttons)

        card.addView(mainLayout)

        return card
    }

    private fun getDestinationImage(destination: String): String {
        return when (destination.trim().lowercase(Locale.ROOT)) {

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

            "istanbul" -> "istanbul"
            "tromsø", "tromso" -> "tromso"
            "sofia", "софия" -> "sofia"

            "moscow" -> "moscow"
            "lisbon" -> "lisbon"
            "bansko" -> "bansko"
            "madrid" -> "madrid"

            else -> "travel"
        }
    }

    private fun createActionButton(
        label: String,
        backgroundColor: Int,
        textColor: Int,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            isAllCaps = false
            setTextColor(textColor)

            setBackgroundTintList(
                ColorStateList.valueOf(backgroundColor)
            )

            minWidth = 0
            minimumWidth = 0
            setPadding(dp(2), 0, dp(2), 0)

            setOnClickListener {
                action()
            }
        }
    }

    private fun createDetailText(value: String): TextView {
        return TextView(this).apply {
            text = value
            textSize = 14f
            setTextColor(appSecondaryText)
            setPadding(0, dp(5), 0, dp(5))
        }
    }

    private fun openEditTrip(trip: TripEntity) {
        val intent = Intent(this, NewTripActivity::class.java)
        intent.putExtra(NewTripActivity.EXTRA_TRIP_ID, trip.id)
        startActivity(intent)
    }

    private fun showDeleteConfirmation(trip: TripEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete Trip")
            .setMessage(
                "Are you sure you want to delete ${trip.destination}?"
            )
            .setPositiveButton("Delete") { _, _ ->
                tripViewModel.deleteTrip(trip)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openInMaps(trip: TripEntity) {
        val location = "${trip.destination}, ${trip.country}"

        val uri = Uri.parse(
            "https://www.google.com/maps/search/?api=1&query=" +
                    Uri.encode(location)
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun parseDate(date: String): Date? {
        return try {
            dateFormat.parse(date)
        } catch (e: Exception) {
            null
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
