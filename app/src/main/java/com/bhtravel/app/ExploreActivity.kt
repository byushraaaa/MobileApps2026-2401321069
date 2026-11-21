
package com.bhtravel.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ExploreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Santorini map
        findViewById<MaterialButton>(R.id.btnSantoriniMap)
            .setOnClickListener {
                openMap("Santorini, Greece")
            }

        // Rome map
        findViewById<MaterialButton>(R.id.btnRomeMap)
            .setOnClickListener {
                openMap("Rome, Italy")
            }

        // Budapest map
        findViewById<MaterialButton>(R.id.btnBudapestMap)
            .setOnClickListener {
                openMap("Budapest, Hungary")
            }
    }

    private fun openMap(destination: String) {
        val url = "https://www.google.com/maps/search/?api=1&query=" +
                Uri.encode(destination)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}
