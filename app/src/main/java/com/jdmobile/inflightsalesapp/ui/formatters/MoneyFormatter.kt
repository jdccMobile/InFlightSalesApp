package com.jdmobile.inflightsalesapp.ui.formatters

import com.jdmobile.inflightsalesapp.domain.model.Currency
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

object MoneyFormatter {
    private const val SCALE = 2
    private val ROUNDING_MODE = RoundingMode.HALF_UP

    fun format(amount: BigDecimal, currency: Currency): String {
        val locale = currency.getLocale()
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            this.currency = java.util.Currency.getInstance(currency.code)
            minimumFractionDigits = SCALE
            maximumFractionDigits = SCALE
        }

        return formatter.format(amount.setScale(SCALE, ROUNDING_MODE))
    }

    fun round(amount: BigDecimal): BigDecimal {
        return amount.setScale(SCALE, ROUNDING_MODE)
    }
}

fun Currency.formatPrice(amount: BigDecimal): String {
    return MoneyFormatter.format(amount, this)
}

fun Currency.getLocale(): Locale {
    return when (this) {
        Currency.USD -> Locale.US
        Currency.EUR -> Locale("es", "ES") // o Locale.FRANCE
        Currency.GBP -> Locale.UK
    }
}

val Currency.code: String
    get() = when (this) {
        Currency.USD -> "USD"
        Currency.EUR -> "EUR"
        Currency.GBP -> "GBP"
    }

operator fun BigDecimal.times(other: BigDecimal): BigDecimal {
    return MoneyFormatter.round(this.multiply(other))
}

operator fun BigDecimal.times(other: Int): BigDecimal {
    return MoneyFormatter.round(this.multiply(BigDecimal(other)))
}
