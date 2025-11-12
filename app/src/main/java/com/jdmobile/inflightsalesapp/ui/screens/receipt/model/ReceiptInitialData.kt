package com.jdmobile.inflightsalesapp.ui.screens.receipt.model

import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CartItem

data class ReceiptInitialData(
    val cart: List<CartItem>,
    val currency: Currency
)
