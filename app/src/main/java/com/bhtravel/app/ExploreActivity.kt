
package com.bhtravel.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class ExploreActivity : AppCompatActivity() {
    private val locationManager by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }
    private var locationListener: LocationListener? = null
    private var locationCancellation: CancellationSignal? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 42
    }

    private data class Destination(
        val cardId: Int,
        val name: String,
        val category: String,
    )

    private val destinations =
        listOf(
            Destination(R.id.cardSantorini, "santorini greece", "beach"),
            Destination(R.id.cardRome, "rome italy", "city culture"),
            Destination(R.id.cardBudapest, "budapest hungary", "city"),
            Destination(R.id.cardBarcelona, "barcelona spain", "beach city"),
            Destination(R.id.cardEgypt, "egypt giza pyramids", "culture"),
        )

    private var currentQuery = ""
    private var currentCategory = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnSearchExplore)
            .setOnClickListener {
                showSearchDialog()
            }

        findViewById<MaterialButton>(R.id.btnMyLocation).setOnClickListener {
            requestCurrentLocation()
        }

        // Category cards

        findViewById<View>(R.id.cardBeach).setOnClickListener {
            openCategory("Beach")
        }

        findViewById<View>(R.id.cardCity).setOnClickListener {
            openCategory("City")
        }

        findViewById<View>(R.id.cardCulture).setOnClickListener {
            openCategory("Culture")
        }

        findViewById<View>(R.id.cardNature).setOnClickListener {
            openCategory("Nature & Mountains")
        }

        // Google Maps buttons
        findViewById<MaterialButton>(R.id.btnSantoriniMap)
            .setOnClickListener {
                openMap("Santorini, Greece")
            }

        findViewById<MaterialButton>(R.id.btnRomeMap)
            .setOnClickListener {
                openMap("Rome, Italy")
            }

        findViewById<MaterialButton>(R.id.btnBudapestMap)
            .setOnClickListener {
                openMap("Budapest, Hungary")
            }

        findViewById<MaterialButton>(R.id.btnBarcelonaMap)
            .setOnClickListener {
                openMap("Barcelona, Spain")
            }

        findViewById<MaterialButton>(R.id.btnEgyptMap)
            .setOnClickListener {
                openMap("Giza Pyramids, Egypt")
            }
    }

    private fun requestCurrentLocation() {
        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
                LOCATION_PERMISSION_REQUEST,
            )
            return
        }

        val provider =
            listOfNotNull(
                if (fineGranted) LocationManager.GPS_PROVIDER else null,
                LocationManager.NETWORK_PROVIDER,
            ).firstOrNull { providerName ->
                runCatching { locationManager.isProviderEnabled(providerName) }.getOrDefault(false)
            }

        if (provider == null) {
            showLocationError("Turn on location services and try again.")
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationCancellation = CancellationSignal()
                locationManager.getCurrentLocation(provider, locationCancellation, mainExecutor) { location ->
                    if (location == null) {
                        showLocationError("Could not get your location. Try again.")
                    } else {
                        openCurrentLocationInMaps(location)
                    }
                }
            } else {
                val listener =
                    LocationListener { location ->
                        locationListener?.let(locationManager::removeUpdates)
                        locationListener = null
                        openCurrentLocationInMaps(location)
                    }
                locationListener = listener
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            }
        } catch (_: SecurityException) {
            showLocationError("Location permission is required to continue.")
        } catch (_: IllegalArgumentException) {
            showLocationError("Could not get your location. Try again.")
        }
    }

    private fun openCurrentLocationInMaps(location: Location) {
        val uri = Uri.parse(MapLocationUri.forCoordinates(location.latitude, location.longitude))
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun showLocationError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
                requestCurrentLocation()
            } else {
                showLocationError("Location permission is needed to open your position in Maps.")
            }
        }
    }

    override fun onStop() {
        locationCancellation?.cancel()
        locationCancellation = null
        locationListener?.let(locationManager::removeUpdates)
        locationListener = null
        super.onStop()
    }

    private fun selectCategory(category: String) {
        currentCategory = category
        currentQuery = ""
        applyFilters()

        if (category == "nature") {
            Toast
                .makeText(
                    this,
                    "Nature and mountain destinations will be added soon!",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    private fun showSearchDialog() {
        val searchInput =
            EditText(this).apply {
                hint = "Enter a destination"
                setSingleLine(true)
                setPadding(40, 30, 40, 30)
                setText(currentQuery)
            }

        AlertDialog
            .Builder(this)
            .setTitle("Search destinations")
            .setView(searchInput)
            .setPositiveButton("Search") { _, _ ->
                currentQuery = searchInput.text.toString().trim()
                currentCategory = ""
                applyFilters()
            }.setNegativeButton("Cancel", null)
            .setNeutralButton("Show all") { _, _ ->
                currentQuery = ""
                currentCategory = ""
                applyFilters()
            }.show()
    }

    private fun applyFilters() {
        var visibleCount = 0

        destinations.forEach { destination ->
            val card = findViewById<View>(destination.cardId)

            val matchesSearch =
                currentQuery.isBlank() ||
                    destination.name.contains(
                        currentQuery,
                        ignoreCase = true,
                    )

            val matchesCategory =
                currentCategory.isBlank() ||
                    destination.category.split(" ").contains(currentCategory)

            val isVisible = matchesSearch && matchesCategory

            card.visibility =
                if (isVisible) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

            if (isVisible) visibleCount++
        }

        if (visibleCount == 0 && currentCategory != "nature") {
            Toast
                .makeText(
                    this,
                    "No matching destinations found.",
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    private fun openCategory(category: String) {
        val intent = Intent(this, CategoryDestinationsActivity::class.java)
        intent.putExtra(CategoryDestinationsActivity.EXTRA_CATEGORY, category)
        startActivity(intent)
    }

    private fun openMap(destination: String) {
        val url =
            "https://www.google.com/maps/search/?api=1&query=" +
                Uri.encode(destination)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
