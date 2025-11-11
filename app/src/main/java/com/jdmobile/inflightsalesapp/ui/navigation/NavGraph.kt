package com.jdmobile.inflightsalesapp.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Product : Route

    @Serializable
    data class Receipt(
        val selectedProducts: String,
        val currency: String,
        val customerType: String,
    ) : Route
}