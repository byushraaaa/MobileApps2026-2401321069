
package com.bhtravel.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class BHtravelApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val preferences = getSharedPreferences(
            "BHtravelSettings",
            MODE_PRIVATE
        )

        val savedTheme = preferences.getInt(
            "app_theme",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )

        AppCompatDelegate.setDefaultNightMode(savedTheme)
    }
}
