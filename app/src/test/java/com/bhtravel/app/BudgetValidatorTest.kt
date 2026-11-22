
package com.bhtravel.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetValidatorTest {

    @Test
    fun emptyCategory_returnsCategoryRequired() {
        val result = BudgetValidator.validate(
            category = "",
            amountText = "25",
            date = "22.11.2026"
        )

        assertEquals(
            BudgetValidationError.CATEGORY_REQUIRED,
            result
        )
    }

    @Test
    fun invalidAmount_returnsInvalidAmount() {
        val result = BudgetValidator.validate(
            category = "Food",
            amountText = "abc",
            date = "22.11.2026"
        )

        assertEquals(
            BudgetValidationError.INVALID_AMOUNT,
            result
        )
    }

    @Test
    fun zeroAmount_returnsInvalidAmount() {
        val result = BudgetValidator.validate(
            category = "Food",
            amountText = "0",
            date = "22.11.2026"
        )

        assertEquals(
            BudgetValidationError.INVALID_AMOUNT,
            result
        )
    }

    @Test
    fun negativeAmount_returnsInvalidAmount() {
        val result = BudgetValidator.validate(
            category = "Food",
            amountText = "-10",
            date = "22.11.2026"
        )

        assertEquals(
            BudgetValidationError.INVALID_AMOUNT,
            result
        )
    }

    @Test
    fun emptyDate_returnsDateRequired() {
        val result = BudgetValidator.validate(
            category = "Food",
            amountText = "25",
            date = ""
        )

        assertEquals(
            BudgetValidationError.DATE_REQUIRED,
            result
        )
    }

    @Test
    fun validExpense_returnsNull() {
        val result = BudgetValidator.validate(
            category = "Food",
            amountText = "25",
            date = "22.11.2026"
        )

        assertNull(result)
    }

    @Test
    fun calculateTotal_returnsCorrectSum() {
        val result = BudgetValidator.calculateTotal(
            listOf(25.0, 15.5, 10.0)
        )

        assertEquals(50.5, result, 0.001)
    }

    @Test
    fun calculateTotal_emptyList_returnsZero() {
        val result = BudgetValidator.calculateTotal(emptyList())

        assertEquals(0.0, result, 0.001)
    }
}
