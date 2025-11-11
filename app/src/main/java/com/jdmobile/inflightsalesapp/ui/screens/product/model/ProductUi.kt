package com.jdmobile.inflightsalesapp.ui.screens.product.model

import com.jdmobile.inflightsalesapp.domain.model.Product
import com.jdmobile.inflightsalesapp.domain.model.ProductId

data class ProductUi(
    val id: ProductId,
    val name: String,
    val stock: Int,
    val priceUSD: Double,
    val priceEUR: Double,
    val priceGBP: Double,
    val imageUrl: String,
    val unitsSelected: Int = 0,
    val category: ProductFilter = ProductFilter.ALL
) {
    fun getPriceForCurrency(currency: Currency): Double {
        return when (currency) {
            Currency.USD -> priceUSD
            Currency.EUR -> priceEUR
            Currency.GBP -> priceGBP
        }
    }

    fun getFormattedPrice(currency: Currency): String {
        val price = getPriceForCurrency(currency)
        return String.format("%.2f %s", price, currency.symbol)
    }
}

fun Product.toUi() = ProductUi(
    id = id,
    name = name,
    stock = stock,
    priceUSD = priceUSD,
    priceEUR = priceEUR,
    priceGBP = priceGBP,
    imageUrl = imageUrl,
    category = category
)
