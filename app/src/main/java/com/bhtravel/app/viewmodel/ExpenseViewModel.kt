
package com.bhtravel.app.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.bhtravel.app.data.entity.ExpenseEntity
import com.bhtravel.app.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
) : ViewModel() {
    fun getExpensesForTrip(tripId: Int): LiveData<List<ExpenseEntity>> = repository.getExpensesForTrip(tripId).asLiveData()

    fun getTotalForTrip(tripId: Int): LiveData<Double?> = repository.getTotalForTrip(tripId).asLiveData()

    fun insertExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.insertExpense(expense)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}
