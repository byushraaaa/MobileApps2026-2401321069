
package com.bhtravel.app

import android.widget.TextView
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class WalletActivityTest {
    @Test
    fun walletActivity_launchesSuccessfully() {
        val activity =
            Robolectric
                .buildActivity(WalletActivity::class.java)
                .setup()
                .get()

        assertNotNull(activity)
        assertNotNull(activity.findViewById<TextView>(R.id.tvTotalBudget))
        assertNotNull(activity.findViewById<TextView>(R.id.tvTotalSpent))
        assertNotNull(activity.findViewById<TextView>(R.id.tvRemainingBalance))
        assertNotNull(activity.findViewById(R.id.budgetProgress))
        assertNotNull(activity.findViewById(R.id.expensesContainer))
    }

    @Test
    fun walletActivity_backButton_finishesActivity() {
        val activity =
            Robolectric
                .buildActivity(WalletActivity::class.java)
                .setup()
                .get()

        activity
            .findViewById<TextView>(R.id.btnBackWallet)
            .performClick()

        assertTrue(activity.isFinishing)
    }

    @Test
    fun walletActivity_setBudgetButton_opensDialog() {
        val activity =
            Robolectric
                .buildActivity(WalletActivity::class.java)
                .setup()
                .get()

        activity
            .findViewById<android.view.View>(R.id.btnSetBudget)
            .performClick()

        val dialog = ShadowAlertDialog.getLatestAlertDialog()

        assertNotNull(dialog)
        assertTrue(dialog.isShowing)
    }

    @Test
    fun walletActivity_addExpenseButton_exists() {
        val activity =
            Robolectric
                .buildActivity(WalletActivity::class.java)
                .setup()
                .get()

        val button =
            activity.findViewById<android.view.View>(
                R.id.btnAddExpense,
            )

        assertNotNull(button)
    }

    @Test
    fun walletActivity_viewAllExpensesButton_exists() {
        val activity =
            Robolectric
                .buildActivity(WalletActivity::class.java)
                .setup()
                .get()

        val button =
            activity.findViewById<TextView>(
                R.id.btnViewAllExpenses,
            )

        assertNotNull(button)
    }
}
