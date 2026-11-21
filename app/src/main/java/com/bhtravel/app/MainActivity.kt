
package com.bhtravel.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // New Trip button
        val btnNewTrip = findViewById<TextView>(R.id.btnNewTrip)
        btnNewTrip.setOnClickListener {
            startActivity(Intent(this, NewTripActivity::class.java))
        }

        // Trips navigation
        val navTrips = findViewById<TextView>(R.id.navTrips)
        navTrips.setOnClickListener {
            startActivity(Intent(this, TripsActivity::class.java))
        }

        // Itinerary button
        val btnItinerary = findViewById<TextView>(R.id.btnItinerary)
        btnItinerary.setOnClickListener {
            startActivity(Intent(this, ItineraryActivity::class.java))
        }

        // Budget button
        val btnBudget = findViewById<TextView>(R.id.btnBudget)
        btnBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // Explore quick action
        val btnExplore = findViewById<TextView>(R.id.btnExplore)
        btnExplore.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        // Explore bottom navigation
        val navExplore = findViewById<TextView>(R.id.navExplore)
        navExplore.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        // Budget bottom navigation
        val navBudget = findViewById<TextView>(R.id.navBudget)
        navBudget.setOnClickListener {
            startActivity(Intent(this, BudgetActivity::class.java))
        }

        // Profile bottom navigation
        val navProfile = findViewById<TextView>(R.id.navProfile)
        navProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

    }
}
