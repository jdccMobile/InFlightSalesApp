package com.jdmobile.inflightsalesapp.domain.service

import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.domain.model.CustomerType
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import java.math.BigDecimal

class PriceCalculator {

    fun calculateFinalPrice(
        product: ProductUi,
        currency: Currency,
        customerType: CustomerType
    ): BigDecimal {
        val basePrice = getBasePrice(product, currency)
        val discount = customerType.discount.toBigDecimal()

        return basePrice * discount
    }

    private fun getBasePrice(product: ProductUi, currency: Currency): BigDecimal {
        return when (currency) {
            Currency.USD -> product.priceUSD
            Currency.EUR -> product.priceEUR
            Currency.GBP -> product.priceGBP
        }
    }
}
