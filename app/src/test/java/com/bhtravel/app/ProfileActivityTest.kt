package com.bhtravel.app

import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.button.MaterialButtonToggleGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ProfileActivityTest {
    @Test
    fun profileActivity_showsThemeOptionsAndWallet() {
        val activity =
            Robolectric
                .buildActivity(ProfileActivity::class.java)
                .setup()
                .get()

        assertNotNull(
            activity.findViewById<MaterialButtonToggleGroup>(R.id.themeToggleGroup),
        )
        assertNotNull(activity.findViewById(R.id.cardWallet))
        assertNotNull(activity.findViewById(R.id.cardFavoriteTrips))
    }

    @Test
    fun selectingDarkTheme_persistsPreference() {
        val activity =
            Robolectric
                .buildActivity(ProfileActivity::class.java)
                .setup()
                .get()

        activity
            .findViewById<MaterialButtonToggleGroup>(R.id.themeToggleGroup)
            .check(R.id.btnDarkTheme)

        assertEquals(
            AppCompatDelegate.MODE_NIGHT_YES,
            activity
                .getSharedPreferences("BHtravelSettings", 0)
                .getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        )
    }

    @Test
    fun savedLightTheme_isSelectedOnLaunch() {
        RuntimeEnvironment
            .getApplication()
            .getSharedPreferences("BHtravelSettings", 0)
            .edit()
            .putInt("app_theme", AppCompatDelegate.MODE_NIGHT_NO)
            .apply()

        val activity =
            Robolectric
                .buildActivity(ProfileActivity::class.java)
                .setup()
                .get()

        val themeGroup =
            activity.findViewById<MaterialButtonToggleGroup>(
                R.id.themeToggleGroup,
            )
        assertEquals(R.id.btnLightTheme, themeGroup.checkedButtonId)
    }
}
