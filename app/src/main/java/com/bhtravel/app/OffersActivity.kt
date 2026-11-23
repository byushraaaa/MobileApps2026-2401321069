
package com.bhtravel.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class OffersActivity : AppCompatActivity() {

    private val navyBlue = Color.parseColor("#142B49")
    private val lightBlue = Color.parseColor("#DCE8F5")
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offers)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Tours category
        findViewById<TextView>(R.id.btnTours).setOnClickListener {
            selectTours()
        }

        // Google Flights
        findViewById<TextView>(R.id.btnFlights).setOnClickListener {
            openWebsite("https://www.google.com/travel/flights")
        }

        // Booking.com
        findViewById<TextView>(R.id.btnHotels).setOnClickListener {
            openWebsite("https://www.booking.com")
        }

        // Moscow
        findViewById<MaterialCardView>(R.id.cardMoscow).setOnClickListener {
            openOffer(
                "Moscow",
                "Russia 🇷🇺",
                "Discover the Russian capital, explore its famous Red Square, beautiful architecture and rich cultural history.",
                "4 days",
                "From €349 / person",
                R.drawable.moscow
            )
        }

        // Lisbon
        findViewById<MaterialCardView>(R.id.cardLisbon).setOnClickListener {
            openOffer(
                "Lisbon",
                "Portugal 🇵🇹",
                "Enjoy Lisbon's colorful streets, historic neighborhoods, ocean views and delicious Portuguese cuisine.",
                "5 days",
                "From €399 / person",
                R.drawable.lisbon
            )
        }

        // Cappadocia
        findViewById<MaterialCardView>(R.id.cardCappadocia).setOnClickListener {
            openOffer(
                "Cappadocia",
                "Turkey 🇹🇷",
                "Experience Cappadocia's unique rock formations, magical valleys and unforgettable hot-air balloon views.",
                "3 days",
                "From €299 / person",
                R.drawable.cappadocia
            )
        }

        // Bansko
        findViewById<MaterialCardView>(R.id.cardBansko).setOnClickListener {
            openOffer(
                "Bansko",
                "Bulgaria 🇧🇬",
                "Enjoy a mountain escape with beautiful scenery, fresh air, winter activities and traditional Bulgarian atmosphere.",
                "4 days",
                "From €249 / person",
                R.drawable.bansko
            )
        }

        // Madrid
        findViewById<MaterialCardView>(R.id.cardMadrid).setOnClickListener {
            openOffer(
                "Madrid",
                "Spain 🇪🇸",
                "Explore Madrid's lively streets, impressive museums, beautiful parks and Spanish culture.",
                "4 days",
                "From €329 / person",
                R.drawable.madrid
            )
        }

        // Default selected category
        selectTours()
    }

    private fun selectTours() {
        val tours = findViewById<TextView>(R.id.btnTours)
        val flights = findViewById<TextView>(R.id.btnFlights)
        val hotels = findViewById<TextView>(R.id.btnHotels)

        tours.setBackgroundColor(navyBlue)
        tours.setTextColor(white)

        flights.setBackgroundColor(lightBlue)
        flights.setTextColor(navyBlue)

        hotels.setBackgroundColor(lightBlue)
        hotels.setTextColor(navyBlue)
    }

    private fun openWebsite(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Unable to open the website.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openOffer(
        destination: String,
        country: String,
        description: String,
        duration: String,
        price: String,
        image: Int
    ) {
        val intent = Intent(this, OfferDetailsActivity::class.java).apply {
            putExtra(OfferDetailsActivity.EXTRA_DESTINATION, destination)
            putExtra(OfferDetailsActivity.EXTRA_COUNTRY, country)
            putExtra(OfferDetailsActivity.EXTRA_DESCRIPTION, description)
            putExtra(OfferDetailsActivity.EXTRA_DURATION, duration)
            putExtra(OfferDetailsActivity.EXTRA_PRICE, price)
            putExtra(OfferDetailsActivity.EXTRA_IMAGE, image)
        }

        startActivity(intent)
    }
}
