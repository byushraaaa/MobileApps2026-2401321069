
package com.bhtravel.app.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.bhtravel.app.MainDispatcherRule
import com.bhtravel.app.data.dao.ExpenseDao
import com.bhtravel.app.data.entity.ExpenseEntity
import com.bhtravel.app.data.repository.ExpenseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private class FakeExpenseDao : ExpenseDao {
        val expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())

        override fun getExpensesForTrip(tripId: Int): Flow<List<ExpenseEntity>> =
            expenses.map { list -> list.filter { it.tripId == tripId } }

        override fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenses

        override suspend fun insertExpense(expense: ExpenseEntity) {
            expenses.value = expenses.value + expense
        }

        override suspend fun updateExpense(expense: ExpenseEntity) {
            expenses.value = expenses.value.map {
                if (it.id == expense.id) expense else it
            }
        }

        override suspend fun deleteExpense(expense: ExpenseEntity) {
            expenses.value = expenses.value.filterNot { it.id == expense.id }
        }

        override fun getTotalForTrip(tripId: Int): Flow<Double?> =
            expenses.map { list ->
                list.filter { it.tripId == tripId }
                    .takeIf { it.isNotEmpty() }
                    ?.sumOf { it.amount }
            }
    }

    private fun createExpense(
        id: Int = 1,
        amount: Double = 100.0
    ) = ExpenseEntity(
        id = id,
        tripId = 10,
        category = "Hotel",
        amount = amount,
        description = "Test expense",
        date = "2026-12-10"
    )

    @Test
    fun getExpensesForTrip_returnsExpenses() = runTest {
        val dao = FakeExpenseDao()
        dao.expenses.value = listOf(createExpense())

        val viewModel = ExpenseViewModel(ExpenseRepository(dao))
        val liveData = viewModel.getExpensesForTrip(10)
        val observer = Observer<List<ExpenseEntity>> {}

        liveData.observeForever(observer)
        advanceUntilIdle()

        assertEquals(1, liveData.value?.size)

        liveData.removeObserver(observer)
    }

    @Test
    fun getTotalForTrip_returnsCorrectTotal() = runTest {
        val dao = FakeExpenseDao()
        dao.expenses.value = listOf(
            createExpense(amount = 100.0),
            createExpense(id = 2, amount = 50.0)
        )

        val viewModel = ExpenseViewModel(ExpenseRepository(dao))
        val liveData = viewModel.getTotalForTrip(10)
        val observer = Observer<Double?> {}

        liveData.observeForever(observer)
        advanceUntilIdle()

        assertEquals(150.0, liveData.value!!, 0.001)

        liveData.removeObserver(observer)
    }

    @Test
    fun insertExpense_addsExpense() = runTest {
        val dao = FakeExpenseDao()
        val viewModel = ExpenseViewModel(ExpenseRepository(dao))

        viewModel.insertExpense(createExpense())
        advanceUntilIdle()

        assertEquals(1, dao.expenses.value.size)
    }

    @Test
    fun updateExpense_changesExpense() = runTest {
        val dao = FakeExpenseDao()
        dao.expenses.value = listOf(createExpense())

        val viewModel = ExpenseViewModel(ExpenseRepository(dao))
        viewModel.updateExpense(createExpense(amount = 250.0))
        advanceUntilIdle()

        assertEquals(250.0, dao.expenses.value.first().amount, 0.001)
    }

    @Test
    fun deleteExpense_removesExpense() = runTest {
        val dao = FakeExpenseDao()
        dao.expenses.value = listOf(createExpense())

        val viewModel = ExpenseViewModel(ExpenseRepository(dao))
        viewModel.deleteExpense(createExpense())
        advanceUntilIdle()

        assertTrue(dao.expenses.value.isEmpty())
    }
}
