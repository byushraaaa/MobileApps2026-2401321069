
package com.bhtravel.app.data.repository

import com.bhtravel.app.data.dao.ExpenseDao
import com.bhtravel.app.data.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
) {
    fun getExpensesForTrip(tripId: Int): Flow<List<ExpenseEntity>> = expenseDao.getExpensesForTrip(tripId)

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()

    fun getTotalForTrip(tripId: Int): Flow<Double?> = expenseDao.getTotalForTrip(tripId)

    suspend fun insertExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}
