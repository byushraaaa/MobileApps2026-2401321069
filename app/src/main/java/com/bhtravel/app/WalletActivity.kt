
package com.bhtravel.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.ExpenseEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalletActivity : AppCompatActivity() {
    private val settings by lazy {
        getSharedPreferences("BHtravelWallet", MODE_PRIVATE)
    }

    private val database by lazy {
        DatabaseProvider.getDatabase(this)
    }

    private lateinit var tvTotalBudget: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var tvRemainingBalance: TextView
    private lateinit var tvBudgetPercentage: TextView
    private lateinit var budgetProgress: ProgressBar
    private lateinit var expensesContainer: LinearLayout
    private lateinit var tvEmptyExpenses: TextView

    private val categories =
        listOf(
            "Flights",
            "Accommodation",
            "Food",
            "Transport",
            "Activities",
            "Shopping",
            "Other",
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        tvTotalBudget = findViewById(R.id.tvTotalBudget)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        tvRemainingBalance = findViewById(R.id.tvRemainingBalance)
        tvBudgetPercentage = findViewById(R.id.tvBudgetPercentage)
        budgetProgress = findViewById(R.id.budgetProgress)
        expensesContainer = findViewById(R.id.expensesContainer)
        tvEmptyExpenses = findViewById(R.id.tvEmptyExpenses)

        findViewById<TextView>(R.id.btnBackWallet).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnSetBudget).setOnClickListener {
            showBudgetDialog()
        }

        findViewById<MaterialButton>(R.id.btnAddExpense).setOnClickListener {
            showExpenseDialog()
        }

        findViewById<TextView>(R.id.btnViewAllExpenses).setOnClickListener {
            loadExpenses(showAll = true)
        }

        loadExpenses()
    }

    override fun onResume() {
        super.onResume()
        if (::expensesContainer.isInitialized) {
            loadExpenses()
        }
    }

    private fun getBudget(): Double = settings.getFloat("total_budget", 0f).toDouble()

    private fun formatMoney(amount: Double): String = String.format(Locale.US, "€%.2f", amount)

    private fun showBudgetDialog() {
        val input =
            EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                hint = "Enter budget in €"
                if (getBudget() > 0) setText(getBudget().toString())
            }

        AlertDialog
            .Builder(this)
            .setTitle("Set Travel Budget")
            .setMessage("Enter your total travel budget.")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val amount =
                    input.text
                        .toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (amount == null || amount <= 0) {
                    Toast
                        .makeText(
                            this,
                            "Enter a valid budget.",
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    settings
                        .edit()
                        .putFloat("total_budget", amount.toFloat())
                        .apply()
                    loadExpenses()
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun showExpenseDialog() {
        lifecycleScope.launch {
            try {
                val tripList = database.tripDao().getAllTrips().first()

                if (tripList.isEmpty()) {
                    Toast
                        .makeText(
                            this@WalletActivity,
                            "Please create a trip before adding an expense.",
                            Toast.LENGTH_LONG,
                        ).show()
                    return@launch
                }

                val tripSpinner = Spinner(this@WalletActivity)
                tripSpinner.adapter =
                    ArrayAdapter(
                        this@WalletActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        tripList.map { "${it.destination}, ${it.country}" },
                    )

                val categorySpinner = Spinner(this@WalletActivity)
                categorySpinner.adapter =
                    ArrayAdapter(
                        this@WalletActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        categories,
                    )

                val amountInput =
                    EditText(this@WalletActivity).apply {
                        hint = "Amount (€)"
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    }

                val dateInput =
                    EditText(this@WalletActivity).apply {
                        hint = "Date (YYYY-MM-DD)"
                        setText(
                            SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault(),
                            ).format(Date()),
                        )
                    }

                val layout =
                    LinearLayout(this@WalletActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(45, 10, 45, 0)
                        addView(makeLabel("Choose trip"))
                        addView(tripSpinner)
                        addView(makeLabel("Category"))
                        addView(categorySpinner)
                        addView(makeLabel("Amount (€)"))
                        addView(amountInput)
                        addView(makeLabel("Date"))
                        addView(dateInput)
                    }

                val dialog =
                    AlertDialog
                        .Builder(this@WalletActivity)
                        .setTitle("Add Expense")
                        .setView(layout)
                        .setPositiveButton("Save", null)
                        .setNegativeButton("Cancel", null)
                        .create()

                dialog.setOnShowListener {
                    dialog
                        .getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val amount =
                                amountInput.text
                                    .toString()
                                    .replace(",", ".")
                                    .toDoubleOrNull()

                            val date = dateInput.text.toString().trim()

                            if (amount == null || amount <= 0) {
                                amountInput.error = "Enter a valid amount"
                                return@setOnClickListener
                            }

                            if (!date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                                dateInput.error = "Use YYYY-MM-DD"
                                return@setOnClickListener
                            }

                            val selectedTrip =
                                tripList[tripSpinner.selectedItemPosition]

                            val selectedCategory =
                                categorySpinner.selectedItem.toString()

                            lifecycleScope.launch {
                                database.expenseDao().insertExpense(
                                    ExpenseEntity(
                                        tripId = selectedTrip.id,
                                        category = selectedCategory,
                                        amount = amount,
                                        description = selectedCategory,
                                        date = date,
                                    ),
                                )
                                loadExpenses()
                            }

                            dialog.dismiss()
                        }
                }

                dialog.show()
            } catch (e: Exception) {
                Toast
                    .makeText(
                        this@WalletActivity,
                        "Unable to load trips.",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    private fun makeLabel(text: String): TextView =
        TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(android.graphics.Color.rgb(20, 43, 73))
            setPadding(0, 16, 0, 4)
        }

    private fun loadExpenses(showAll: Boolean = false) {
        lifecycleScope.launch {
            try {
                val expenses = database.expenseDao().getAllExpenses().first()
                val budget = getBudget()
                val totalSpent = expenses.sumOf { it.amount }
                val remaining = budget - totalSpent

                tvTotalBudget.text = formatMoney(budget)
                tvTotalSpent.text = formatMoney(totalSpent)
                tvRemainingBalance.text = formatMoney(remaining)

                val percentage =
                    if (budget > 0) {
                        ((totalSpent / budget) * 100).toInt().coerceAtLeast(0)
                    } else {
                        0
                    }

                budgetProgress.progress = percentage.coerceAtMost(100)
                tvBudgetPercentage.text = "$percentage% of your budget used"

                expensesContainer.removeAllViews()

                val visibleExpenses = if (showAll) expenses else expenses.take(5)

                tvEmptyExpenses.visibility =
                    if (expenses.isEmpty()) View.VISIBLE else View.GONE

                visibleExpenses.forEach { expense ->
                    expensesContainer.addView(createExpenseCard(expense))
                }

                expensesContainer.addView(tvEmptyExpenses)
            } catch (e: Exception) {
                Toast
                    .makeText(
                        this@WalletActivity,
                        "Unable to load expenses.",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    private fun createExpenseCard(expense: ExpenseEntity): View {
        val card =
            MaterialCardView(this).apply {
                radius = 18f * resources.displayMetrics.density
                cardElevation = 2f
                setCardBackgroundColor(android.graphics.Color.WHITE)
                strokeWidth = (resources.displayMetrics.density).toInt()
                strokeColor = android.graphics.Color.rgb(220, 232, 245)
                layoutParams =
                    LinearLayout
                        .LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            bottomMargin = (10 * resources.displayMetrics.density).toInt()
                        }
            }

        val row =
            LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16, 14, 16, 14)
            }

        val details =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

        val title =
            TextView(this).apply {
                text = expense.category
                textSize = 15f
                setTextColor(android.graphics.Color.rgb(20, 43, 73))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

        val subtitle =
            TextView(this).apply {
                text = expense.date
                textSize = 12f
                setTextColor(android.graphics.Color.rgb(113, 128, 150))
            }

        val amount =
            TextView(this).apply {
                text = "-${formatMoney(expense.amount)}"
                textSize = 15f
                setTextColor(android.graphics.Color.rgb(20, 43, 73))
                setTypeface(null, android.graphics.Typeface.BOLD)
                gravity = Gravity.END
            }

        details.addView(title)
        details.addView(subtitle)

        row.addView(
            details,
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f,
            ),
        )
        row.addView(amount)
        card.addView(row)

        card.setOnClickListener {
            showExpenseActions(expense)
        }

        return card
    }

    private fun showExpenseActions(expense: ExpenseEntity) {
        val options = arrayOf("Edit expense", "Delete expense", "Cancel")

        AlertDialog
            .Builder(this)
            .setTitle(expense.category)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditExpenseDialog(expense)
                    1 -> confirmDeleteExpense(expense)
                }
            }.show()
    }

    private fun showEditExpenseDialog(expense: ExpenseEntity) {
        val categorySpinner = Spinner(this)
        categorySpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                categories,
            )

        val categoryIndex = categories.indexOf(expense.category)
        if (categoryIndex >= 0) {
            categorySpinner.setSelection(categoryIndex)
        }

        val amountInput =
            EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                setText(expense.amount.toString())
            }

        val layout =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(45, 10, 45, 0)
                addView(makeLabel("Category"))
                addView(categorySpinner)
                addView(makeLabel("Amount (€)"))
                addView(amountInput)
            }

        AlertDialog
            .Builder(this)
            .setTitle("Edit Expense")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val amount =
                    amountInput.text
                        .toString()
                        .replace(",", ".")
                        .toDoubleOrNull()

                if (amount == null || amount <= 0) {
                    Toast
                        .makeText(
                            this,
                            "Enter a valid amount.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    return@setPositiveButton
                }

                val newCategory = categorySpinner.selectedItem.toString()

                lifecycleScope.launch {
                    database.expenseDao().updateExpense(
                        expense.copy(
                            category = newCategory,
                            description = newCategory,
                            amount = amount,
                        ),
                    )
                    loadExpenses()
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteExpense(expense: ExpenseEntity) {
        AlertDialog
            .Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    database.expenseDao().deleteExpense(expense)
                    loadExpenses()
                }
            }.setNegativeButton("Cancel", null)
            .show()
    }
}
