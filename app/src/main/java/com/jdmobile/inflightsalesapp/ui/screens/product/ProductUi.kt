package com.jdmobile.inflightsalesapp.ui.screens.product

data class ProductUi(
    val id: String,
    val name: String,
    val unit: String,
    val priceUSD: Double,
    val priceEUR: Double,
    val priceGBP: Double,
    val imageUrl: String,
    val quantity: Int = 0,
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

enum class ProductFilter {
    ALL,
    FOOD,
    BEVERAGES
}

enum class Currency(val symbol: String, val label: String) {
    USD("$", "USD"),
    EUR("€", "EUR"),
    GBP("£", "GBP")
}