
package com.bhtravel.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create a New Trip
        findViewById<TextView>(R.id.btnNewTrip).setOnClickListener {
            startActivity(Intent(this, NewTripActivity::class.java))
        }

        // Find Flights
        findViewById<TextView>(R.id.btnFindFlights).setOnClickListener {
            openWebsite("https://www.google.com/travel/flights")
        }

        // Book Hotels
        findViewById<TextView>(R.id.btnBookHotels).setOnClickListener {
            openWebsite("https://www.booking.com")
        }

        // Rent a Car
        findViewById<TextView>(R.id.btnRentCar).setOnClickListener {
            openWebsite("https://www.rentalcars.com")
        }

        // My Trips
        findViewById<TextView>(R.id.navTrips).setOnClickListener {
            startActivity(Intent(this, TripsActivity::class.java))
        }

        // Offers
        findViewById<TextView>(R.id.navOffers).setOnClickListener {
            startActivity(Intent(this, OffersActivity::class.java))
        }

        // Explore
        findViewById<TextView>(R.id.navExplore).setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        // Profile
        findViewById<TextView>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun openWebsite(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
