
package com.bhtravel.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.FavoriteOfferEntity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class OfferDetailsActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_COUNTRY = "extra_country"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_DURATION = "extra_duration"
        const val EXTRA_PRICE = "extra_price"
        const val EXTRA_IMAGE = "extra_image"
    }

    private lateinit var favoriteButton: MaterialButton
    private lateinit var favoriteOffer: FavoriteOfferEntity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offer_details)

        val destination =
            intent.getStringExtra(EXTRA_DESTINATION) ?: "Destination"

        val country =
            intent.getStringExtra(EXTRA_COUNTRY) ?: ""

        val description =
            intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""

        val duration =
            intent.getStringExtra(EXTRA_DURATION) ?: ""

        val price =
            intent.getStringExtra(EXTRA_PRICE) ?: ""

        val imageRes =
            intent.getIntExtra(EXTRA_IMAGE, R.drawable.travel)

        findViewById<ImageView>(R.id.ivOfferImage)
            .setImageResource(imageRes)

        findViewById<TextView>(R.id.tvOfferDestination).text = destination
        findViewById<TextView>(R.id.tvOfferCountry).text = country
        findViewById<TextView>(R.id.tvOfferDescription).text = description
        findViewById<TextView>(R.id.tvOfferDuration).text = duration
        findViewById<TextView>(R.id.tvOfferPrice).text = price

        favoriteButton = findViewById(R.id.btnAddFavorite)

        val imageName =
            try {
                resources.getResourceEntryName(imageRes)
            } catch (e: Exception) {
                "travel"
            }

        favoriteOffer =
            FavoriteOfferEntity(
                destination = destination,
                country = country,
                description = description,
                duration = duration,
                price = price,
                imageName = imageName,
            )

        val dao = DatabaseProvider.getDatabase(this).favoriteOfferDao()

        // Check whether the offer is already a favorite.
        lifecycleScope.launch {
            updateFavoriteButton(dao.isFavorite(destination))
        }

        // Add or remove from Favorites.
        favoriteButton.setOnClickListener {
            lifecycleScope.launch {
                val alreadyFavorite = dao.isFavorite(destination)

                if (alreadyFavorite) {
                    dao.removeFavoriteByDestination(destination)
                    updateFavoriteButton(false)

                    Toast
                        .makeText(
                            this@OfferDetailsActivity,
                            "Removed from Favorites",
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    dao.addFavorite(favoriteOffer)
                    updateFavoriteButton(true)

                    Toast
                        .makeText(
                            this@OfferDetailsActivity,
                            "Added to Favorites!",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }

        // Open New Trip form with destination and country pre-filled.
        findViewById<MaterialButton>(R.id.btnAddToTrips)
            .setOnClickListener {
                val tripIntent =
                    Intent(
                        this,
                        NewTripActivity::class.java,
                    ).apply {
                        putExtra(
                            NewTripActivity.EXTRA_DESTINATION,
                            destination,
                        )

                        putExtra(
                            NewTripActivity.EXTRA_COUNTRY,
                            country,
                        )
                    }

                startActivity(tripIntent)
            }

        // Back button.
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        favoriteButton.text =
            if (isFavorite) {
                "♥  Added to Favorites"
            } else {
                "♡  Add to Favorites"
            }
    }
}
