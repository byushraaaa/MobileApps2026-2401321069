
package com.bhtravel.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CategoryDestinationsActivity : AppCompatActivity() {

    private data class Destination(
        val name: String,
        val country: String,
        val description: String,
        val image: String,
        val mapQuery: String
    )

    private val destinations = mapOf(
        "Beach" to listOf(
            Destination("Santorini", "Greece", "White houses, blue domes and unforgettable sunsets.", "santorini", "Santorini, Greece"),
            Destination("Barcelona", "Spain", "Beautiful beaches and Mediterranean charm.", "barcelona", "Barcelona, Spain"),
            Destination("Lisbon", "Portugal", "Coastal views, colorful streets and ocean atmosphere.", "lisbon", "Lisbon, Portugal"),
            Destination("Istanbul", "Turkey", "Discover the beautiful Bosphorus and waterfront.", "istanbul", "Istanbul, Turkey")
        ),

        "City" to listOf(
            Destination("Rome", "Italy", "Ancient history and impressive architecture.", "rome", "Rome, Italy"),
            Destination("Budapest", "Hungary", "Beautiful bridges, the Danube and thermal baths.", "budapest", "Budapest, Hungary"),
            Destination("Milan", "Italy", "Fashion, shopping and impressive architecture.", "milano", "Milan, Italy"),
            Destination("Vienna", "Austria", "Elegant streets, palaces and coffee culture.", "vienna", "Vienna, Austria"),
            Destination("Amsterdam", "Netherlands", "Canals, museums and charming streets.", "amsterdam", "Amsterdam, Netherlands"),
            Destination("Madrid", "Spain", "Spanish culture, art and lively city squares.", "madrid", "Madrid, Spain"),
            Destination("Moscow", "Russia", "Historic landmarks and impressive architecture.", "moscow", "Moscow, Russia"),
            Destination("Sofia", "Bulgaria", "Discover Bulgarian history and modern city life.", "sofia", "Sofia, Bulgaria"),
            Destination("Paris", "France", "Romantic streets and world-famous landmarks.", "paris", "Paris, France"),
            Destination("Barcelona", "Spain", "Explore the lively streets of Barcelona.", "barcelona", "Barcelona, Spain"),
            Destination("Lisbon", "Portugal", "Discover the colorful capital of Portugal.", "lisbon", "Lisbon, Portugal")
        ),

        "Culture" to listOf(
            Destination("Rome", "Italy", "Explore the Colosseum and ancient Roman history.", "rome", "Rome, Italy"),
            Destination("Istanbul", "Turkey", "A unique meeting point of European and Asian cultures.", "istanbul", "Istanbul, Turkey"),
            Destination("Moscow", "Russia", "Discover historic squares and cultural landmarks.", "moscow", "Moscow, Russia"),
            Destination("Sofia", "Bulgaria", "Explore ancient heritage and Bulgarian traditions.", "sofia", "Sofia, Bulgaria"),
            Destination("Vienna", "Austria", "Discover imperial palaces, museums and classical music.", "vienna", "Vienna, Austria"),
            Destination("Milan", "Italy", "Discover art, design and Italian heritage.", "milano", "Milan, Italy"),
            Destination("Giza Pyramids", "Egypt", "Discover the ancient pyramids and the Great Sphinx.", "egypt", "Giza Pyramids, Egypt")
        ),

        "Nature & Mountains" to listOf(
            Destination("Tromsø", "Norway", "Discover Arctic landscapes and beautiful nature.", "tromso", "Tromso, Norway"),
            Destination("Bansko", "Bulgaria", "Enjoy mountain views and fresh mountain air.", "bansko", "Bansko, Bulgaria"),
            Destination("Cappadocia", "Turkey", "Amazing rock formations and unforgettable landscapes.", "cappadocia", "Cappadocia, Turkey")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Beach"
        val places = destinations[category] ?: emptyList()

        val scrollView = ScrollView(this).apply {
            setBackgroundColor(Color.WHITE)
        }

        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
        }

        val back = TextView(this).apply {
            text = "‹  Back"
            textSize = 15f
            setTextColor(Color.rgb(40, 150, 145))
            setPadding(0, dp(4), 0, dp(12))
            setOnClickListener { finish() }
        }
        mainLayout.addView(back)

        val title = TextView(this).apply {
            text = category
            textSize = 27f
            setTextColor(Color.rgb(20, 43, 73))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        mainLayout.addView(title)

        val subtitle = TextView(this).apply {
            text = "${places.size} destinations to discover"
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, dp(5), 0, dp(18))
        }
        mainLayout.addView(subtitle)

        places.forEach { destination ->
            mainLayout.addView(createDestinationCard(destination))
        }

        scrollView.addView(mainLayout)
        setContentView(scrollView)
    }

    private fun createDestinationCard(destination: Destination): MaterialCardView {
        val card = MaterialCardView(this).apply {
            radius = dp(18).toFloat()
            cardElevation = dp(2).toFloat()
            setCardBackgroundColor(Color.rgb(244, 240, 247))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(16)
            }
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13), dp(13), dp(13), dp(13))
        }

        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(175)
            )
            scaleType = ImageView.ScaleType.CENTER_CROP

            val imageId = resources.getIdentifier(
                destination.image,
                "drawable",
                packageName
            )

            setImageResource(
                if (imageId != 0) imageId else R.drawable.travel
            )
            contentDescription = destination.name
        }
        content.addView(imageView)

        val name = TextView(this).apply {
            text = destination.name
            textSize = 20f
            setTextColor(Color.rgb(20, 43, 73))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(12), 0, dp(3))
        }
        content.addView(name)

        val country = TextView(this).apply {
            text = destination.country
            textSize = 13f
            setTextColor(Color.rgb(40, 150, 145))
        }
        content.addView(country)

        val description = TextView(this).apply {
            text = destination.description
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(8), 0, dp(8))
        }
        content.addView(description)

        val mapButton = MaterialButton(this).apply {
            text = "Explore on Map"
            isAllCaps = false
            cornerRadius = dp(12)
            setBackgroundColor(Color.rgb(47, 183, 176))
            setTextColor(Color.WHITE)

            setOnClickListener {
                val url = "https://www.google.com/maps/search/?api=1&query=" +
                        Uri.encode(destination.mapQuery)

                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        }
        content.addView(mapButton)

        card.addView(content)
        return card
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        const val EXTRA_CATEGORY = "extra_category"
    }
}
