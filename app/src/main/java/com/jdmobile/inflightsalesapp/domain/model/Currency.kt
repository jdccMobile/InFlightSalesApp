package com.jdmobile.inflightsalesapp.domain.model

enum class Currency(val symbol: String, val label: String) {
    USD("$", "USD"),
    EUR("€", "EUR"),
    GBP("£", "GBP")
}