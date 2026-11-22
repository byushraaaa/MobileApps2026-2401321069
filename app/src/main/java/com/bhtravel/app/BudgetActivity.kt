
package com.bhtravel.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.bhtravel.app.data.database.DatabaseProvider
import com.bhtravel.app.data.entity.ExpenseEntity
import com.bhtravel.app.data.entity.TripEntity
import com.bhtravel.app.data.repository.ExpenseRepository
import com.bhtravel.app.data.repository.TripRepository
import com.bhtravel.app.viewmodel.ExpenseViewModel
import com.bhtravel.app.viewmodel.ExpenseViewModelFactory
import com.bhtravel.app.viewmodel.TripViewModel
import com.bhtravel.app.viewmodel.TripViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BudgetActivity : AppCompatActivity() {

    private lateinit var spinnerTrips: Spinner
    private lateinit var etCategory: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var tvTotalExpenses: TextView
    private lateinit var expensesContainer: LinearLayout
    private lateinit var tvEmptyExpenses: TextView

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var tripViewModel: TripViewModel

    private var tripsList: List<TripEntity> = emptyList()
    private var selectedTrip: TripEntity? = null
    private var expensesLiveData: LiveData<List<ExpenseEntity>>? = null

    private val dateFormat =
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        initializeViews()
        initializeViewModels()
        setupListeners()
        observeTrips()

        etDate.setText(dateFormat.format(Calendar.getInstance().time))
    }

    private fun initializeViews() {
        spinnerTrips = findViewById(R.id.spinnerTrips)
        etCategory = findViewById(R.id.etCategory)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etExpenseDescription)
        etDate = findViewById(R.id.etExpenseDate)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        expensesContainer = findViewById(R.id.expensesContainer)
        tvEmptyExpenses = findViewById(R.id.tvEmptyExpenses)
    }

    private fun initializeViewModels() {
        val database = DatabaseProvider.getDatabase(this)

        val tripRepository = TripRepository(database.tripDao())
        val tripFactory = TripViewModelFactory(tripRepository)

        tripViewModel = ViewModelProvider(
            this,
            tripFactory
        )[TripViewModel::class.java]

        val expenseRepository = ExpenseRepository(database.expenseDao())
        val expenseFactory = ExpenseViewModelFactory(expenseRepository)

        expenseViewModel = ViewModelProvider(
            this,
            expenseFactory
        )[ExpenseViewModel::class.java]
    }

    private fun setupListeners() {
        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        etDate.setOnClickListener {
            showDatePicker()
        }

        findViewById<MaterialButton>(R.id.btnAddExpense).setOnClickListener {
            addExpense()
        }
    }

    private fun observeTrips() {
        tripViewModel.allTrips.observe(this) { trips ->
            tripsList = trips

            val tripNames = trips.map {
                "${it.destination}, ${it.country}"
            }

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                tripNames
            )

            adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )

            spinnerTrips.adapter = adapter

            if (trips.isEmpty()) {
                selectedTrip = null
                showEmptyMessage("Add a trip before recording expenses.")
                tvTotalExpenses.text = "€0.00"
            } else {
                spinnerTrips.onItemSelectedListener =
                    object : android.widget.AdapterView.OnItemSelectedListener {

                        override fun onItemSelected(
                            parent: android.widget.AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            selectedTrip = tripsList.getOrNull(position)
                            observeExpenses()
                        }

                        override fun onNothingSelected(
                            parent: android.widget.AdapterView<*>?
                        ) {
                            selectedTrip = null
                        }
                    }
            }
        }
    }

    private fun observeExpenses() {
        val trip = selectedTrip ?: return

        expensesLiveData?.removeObservers(this)

        val liveData = expenseViewModel.getExpensesForTrip(trip.id)
        expensesLiveData = liveData

        liveData.observe(this) { expenses ->
            displayExpenses(expenses)

            val total = BudgetValidator.calculateTotal(
                expenses.map { it.amount }
            )

            tvTotalExpenses.text =
                "€${"%.2f".format(Locale.US, total)}"
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                etDate.setText(dateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun addExpense() {
        val trip = selectedTrip

        if (trip == null) {
            Toast.makeText(
                this,
                "Please select a trip first.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val category = etCategory.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val date = etDate.text.toString().trim()

        val validationError = BudgetValidator.validate(
            category = category,
            amountText = amountText,
            date = date
        )

        when (validationError) {
            BudgetValidationError.CATEGORY_REQUIRED -> {
                etCategory.error = "Enter a category"
                etCategory.requestFocus()
                return
            }

            BudgetValidationError.INVALID_AMOUNT -> {
                etAmount.error = "Enter a valid amount"
                etAmount.requestFocus()
                return
            }

            BudgetValidationError.DATE_REQUIRED -> {
                etDate.error = "Select a date"
                etDate.requestFocus()
                return
            }

            null -> {
                // All fields are valid.
            }
        }

        val amount = amountText.toDoubleOrNull()!!

        val expense = ExpenseEntity(
            tripId = trip.id,
            category = category,
            amount = amount,
            description = description,
            date = date
        )

        expenseViewModel.insertExpense(expense)

        etCategory.text.clear()
        etAmount.text.clear()
        etDescription.text.clear()

        Toast.makeText(
            this,
            "Expense added successfully!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun displayExpenses(expenses: List<ExpenseEntity>) {
        expensesContainer.removeAllViews()

        tvEmptyExpenses.visibility =
            if (expenses.isEmpty()) View.VISIBLE else View.GONE

        for (expense in expenses) {
            val card = MaterialCardView(this).apply {
                radius = 18f
                cardElevation = 2f
                setCardBackgroundColor(android.graphics.Color.WHITE)

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = 12
                layoutParams = params
            }

            val content = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(18, 16, 18, 16)
            }

            val title = TextView(this).apply {
                text = expense.category
                textSize = 17f
                setTextColor(android.graphics.Color.rgb(23, 50, 77))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val description = TextView(this).apply {
                text = if (expense.description.isBlank()) {
                    expense.date
                } else {
                    "${expense.description}\n${expense.date}"
                }
                textSize = 13f
                setTextColor(android.graphics.Color.GRAY)
                setPadding(0, 6, 0, 6)
            }

            val amount = TextView(this).apply {
                text = "€${"%.2f".format(Locale.US, expense.amount)}"
                textSize = 18f
                setTextColor(android.graphics.Color.rgb(32, 166, 166))
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val deleteButton = MaterialButton(
                this,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = "Delete"
                setAllCaps(false)
                setOnClickListener {
                    confirmDelete(expense)
                }
            }

            content.addView(title)
            content.addView(description)
            content.addView(amount)
            content.addView(deleteButton)

            card.addView(content)
            expensesContainer.addView(card)
        }
    }

    private fun confirmDelete(expense: ExpenseEntity) {
        AlertDialog.Builder(this)
            .setTitle("Delete expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->
                expenseViewModel.deleteExpense(expense)

                Toast.makeText(
                    this,
                    "Expense deleted.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun showEmptyMessage(message: String) {
        expensesContainer.removeAllViews()
        tvEmptyExpenses.text = message
        tvEmptyExpenses.visibility = View.VISIBLE
    }
}
