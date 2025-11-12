package com.jdmobile.inflightsalesapp.domain.service

import com.jdmobile.inflightsalesapp.domain.model.CardData
import com.jdmobile.inflightsalesapp.domain.model.ValidationResult
import java.math.BigDecimal

class PaymentValidator {

    fun validateCashPayment(
        cashAmount: String,
        total: BigDecimal
    ): ValidationResult {
        val amount = cashAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        return if (amount >= total) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = "The cash received must be greater than or equal to the total."
            )
        }
    }

    fun validateCardPayment(
        cardData: CardData,
        hasSeatNumber: Boolean
    ): ValidationResult {
        return if (cardData.isComplete() && hasSeatNumber) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = "Please complete all fields"
            )
        }
    }

    fun calculateChange(cashAmount: String, total: BigDecimal): BigDecimal {
        val amount = cashAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val change = amount.subtract(total)
        return if (change < BigDecimal.ZERO) BigDecimal.ZERO else change
    }
}
