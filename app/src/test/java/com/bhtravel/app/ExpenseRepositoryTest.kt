
package com.bhtravel.app.data.repository

import com.bhtravel.app.data.dao.ExpenseDao
import com.bhtravel.app.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExpenseRepositoryTest {

    private class FakeExpenseDao : ExpenseDao {
        private val expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())

        override fun getExpensesForTrip(tripId: Int): Flow<List<ExpenseEntity>> =
            MutableStateFlow(expenses.value.filter { it.tripId == tripId })

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
            MutableStateFlow(
                expenses.value.filter { it.tripId == tripId }.sumOf { it.amount }
                    .takeIf { expenses.value.any { expense -> expense.tripId == tripId } }
            )
    }

    private fun createExpense(
        id: Int = 1,
        tripId: Int = 10,
        category: String = "Hotel",
        amount: Double = 100.0
    ) = ExpenseEntity(
        id = id,
        tripId = tripId,
        category = category,
        amount = amount,
        description = "Test expense",
        date = "2026-12-10"
    )

    @Test
    fun insertExpense_addsExpense() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())
        repository.insertExpense(createExpense())

        assertEquals(1, repository.getAllExpenses().first().size)
    }

    @Test
    fun getExpensesForTrip_returnsOnlyMatchingExpenses() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())
        repository.insertExpense(createExpense())
        repository.insertExpense(createExpense(id = 2, tripId = 20))

        assertEquals(1, repository.getExpensesForTrip(10).first().size)
    }

    @Test
    fun getAllExpenses_returnsAllExpenses() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())
        repository.insertExpense(createExpense())
        repository.insertExpense(createExpense(id = 2))

        assertEquals(2, repository.getAllExpenses().first().size)
    }

    @Test
    fun updateExpense_changesExistingExpense() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())
        repository.insertExpense(createExpense())

        repository.updateExpense(createExpense(amount = 250.0))

        assertEquals(250.0, repository.getAllExpenses().first().first().amount, 0.001)
    }

    @Test
    fun deleteExpense_removesExpense() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())
        val expense = createExpense()
        repository.insertExpense(expense)

        repository.deleteExpense(expense)

        assertTrue(repository.getAllExpenses().first().isEmpty())
    }

    @Test
    fun getTotalForTrip_returnsCorrectTotal() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())
        repository.insertExpense(createExpense(amount = 100.0))
        repository.insertExpense(createExpense(id = 2, amount = 50.0))

        assertEquals(150.0, repository.getTotalForTrip(10).first()!!, 0.001)
    }

    @Test
    fun getTotalForTrip_returnsNullWhenNoExpensesExist() = runBlocking {
        val repository = ExpenseRepository(FakeExpenseDao())

        assertNull(repository.getTotalForTrip(99).first())
    }
}
