
package com.bhtravel.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView

class ProfileActivity : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        preferences = getSharedPreferences("BHtravelSettings", MODE_PRIVATE)

        // Back button
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // Favorite Trips
        findViewById<MaterialCardView>(R.id.cardFavoriteTrips)
            .setOnClickListener {
                startActivity(
                    Intent(this, FavoriteTripsActivity::class.java),
                )
            }

        // My Wallet
        findViewById<MaterialCardView>(R.id.cardWallet)
            .setOnClickListener {
                startActivity(
                    Intent(this, WalletActivity::class.java),
                )
            }

        // Appearance
        setupThemeSelector()
    }

    private fun setupThemeSelector() {
        val themeGroup =
            findViewById<MaterialButtonToggleGroup>(R.id.themeToggleGroup)

        val savedTheme =
            preferences.getInt(
                "app_theme",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            )

        when (savedTheme) {
            AppCompatDelegate.MODE_NIGHT_NO ->
                themeGroup.check(R.id.btnLightTheme)

            AppCompatDelegate.MODE_NIGHT_YES ->
                themeGroup.check(R.id.btnDarkTheme)

            else ->
                themeGroup.check(R.id.btnSystemTheme)
        }

        themeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->

            if (!isChecked) return@addOnButtonCheckedListener

            val selectedTheme =
                when (checkedId) {
                    R.id.btnLightTheme ->
                        AppCompatDelegate.MODE_NIGHT_NO

                    R.id.btnDarkTheme ->
                        AppCompatDelegate.MODE_NIGHT_YES

                    else ->
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

            preferences
                .edit()
                .putInt("app_theme", selectedTheme)
                .apply()

            AppCompatDelegate.setDefaultNightMode(selectedTheme)
        }
    }
}
