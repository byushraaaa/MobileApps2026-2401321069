
package com.bhtravel.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ExploreActivity : AppCompatActivity() {

    private data class Destination(
        val cardId: Int,
        val name: String,
        val category: String
    )

    private val destinations = listOf(
        Destination(R.id.cardSantorini, "santorini greece", "beach"),
        Destination(R.id.cardRome, "rome italy", "city culture"),
        Destination(R.id.cardBudapest, "budapest hungary", "city"),
        Destination(R.id.cardBarcelona, "barcelona spain", "beach city"),
        Destination(R.id.cardEgypt, "egypt giza pyramids", "culture")
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

    private fun selectCategory(category: String) {
        currentCategory = category
        currentQuery = ""
        applyFilters()

        if (category == "nature") {
            Toast.makeText(
                this,
                "Nature and mountain destinations will be added soon!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showSearchDialog() {
        val searchInput = EditText(this).apply {
            hint = "Enter a destination"
            setSingleLine(true)
            setPadding(40, 30, 40, 30)
            setText(currentQuery)
        }

        AlertDialog.Builder(this)
            .setTitle("Search destinations")
            .setView(searchInput)
            .setPositiveButton("Search") { _, _ ->
                currentQuery = searchInput.text.toString().trim()
                currentCategory = ""
                applyFilters()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Show all") { _, _ ->
                currentQuery = ""
                currentCategory = ""
                applyFilters()
            }
            .show()
    }

    private fun applyFilters() {
        var visibleCount = 0

        destinations.forEach { destination ->
            val card = findViewById<View>(destination.cardId)

            val matchesSearch = currentQuery.isBlank() ||
                    destination.name.contains(
                        currentQuery,
                        ignoreCase = true
                    )

            val matchesCategory = currentCategory.isBlank() ||
                    destination.category.split(" ").contains(currentCategory)

            val isVisible = matchesSearch && matchesCategory

            card.visibility = if (isVisible) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (isVisible) visibleCount++
        }

        if (visibleCount == 0 && currentCategory != "nature") {
            Toast.makeText(
                this,
                "No matching destinations found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openCategory(category: String) {
        val intent = Intent(this, CategoryDestinationsActivity::class.java)
        intent.putExtra(CategoryDestinationsActivity.EXTRA_CATEGORY, category)
        startActivity(intent)
    }


    private fun openMap(destination: String) {
        val url = "https://www.google.com/maps/search/?api=1&query=" +
                Uri.encode(destination)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
