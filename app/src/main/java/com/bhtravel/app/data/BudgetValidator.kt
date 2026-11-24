
package com.bhtravel.app

enum class BudgetValidationError {
    CATEGORY_REQUIRED,
    INVALID_AMOUNT,
    DATE_REQUIRED,
}

object BudgetValidator {
    fun validate(
        category: String,
        amountText: String,
        date: String,
    ): BudgetValidationError? {
        if (category.isBlank()) {
            return BudgetValidationError.CATEGORY_REQUIRED
        }

        val amount = amountText.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            return BudgetValidationError.INVALID_AMOUNT
        }

        if (date.isBlank()) {
            return BudgetValidationError.DATE_REQUIRED
        }

        return null
    }

    fun calculateTotal(amounts: List<Double>): Double = amounts.sum()
}
