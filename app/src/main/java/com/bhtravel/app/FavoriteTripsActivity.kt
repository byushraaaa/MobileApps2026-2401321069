
package com.bhtravel.app

import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.FavoriteOfferEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class FavoriteTripsActivity : AppCompatActivity() {

    private lateinit var favoritesContainer: LinearLayout
    private lateinit var emptyMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_trips)

        favoritesContainer = findViewById(R.id.favoritesContainer)
        emptyMessage = findViewById(R.id.tvEmptyFavorites)

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val dao = DatabaseProvider.getDatabase(this).favoriteOfferDao()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dao.getAllFavorites().collect { favorites ->
                    displayFavorites(favorites)
                }
            }
        }
    }

    private fun displayFavorites(favorites: List<FavoriteOfferEntity>) {
        favoritesContainer.removeAllViews()

        if (favorites.isEmpty()) {
            emptyMessage.visibility = TextView.VISIBLE
            return
        }

        emptyMessage.visibility = TextView.GONE

        favorites.forEach { favorite ->
            favoritesContainer.addView(createFavoriteCard(favorite))
        }
    }

    private fun createFavoriteCard(
        favorite: FavoriteOfferEntity
    ): MaterialCardView {

        val card = MaterialCardView(this).apply {
            radius = dp(18).toFloat()
            cardElevation = dp(1).toFloat()
            setCardBackgroundColor(getColor(android.R.color.white))

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }

        val image = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(150)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP

            val imageId = resources.getIdentifier(
                favorite.imageName,
                "drawable",
                packageName
            )

            setImageResource(
                if (imageId != 0) imageId else R.drawable.travel
            )
        }

        val title = TextView(this).apply {
            text = favorite.destination
            textSize = 19f
            setTextColor(getColor(android.R.color.primary_text_light))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(4))
        }

        val country = TextView(this).apply {
            text = favorite.country
            textSize = 14f
        }

        val duration = TextView(this).apply {
            text = favorite.duration
            textSize = 14f
            setPadding(0, dp(8), 0, 0)
        }

        val price = TextView(this).apply {
            text = favorite.price
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(10))
        }

        val removeButton = MaterialButton(this).apply {
            text = "Remove from Favorites"
            isAllCaps = false
            setOnClickListener {
                lifecycleScope.launch {
                    DatabaseProvider.getDatabase(this@FavoriteTripsActivity)
                        .favoriteOfferDao()
                        .removeFavoriteByDestination(favorite.destination)
                }
            }
        }

        content.addView(image)
        content.addView(title)
        content.addView(country)
        content.addView(duration)
        content.addView(price)
        content.addView(removeButton)

        card.addView(content)
        return card
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
