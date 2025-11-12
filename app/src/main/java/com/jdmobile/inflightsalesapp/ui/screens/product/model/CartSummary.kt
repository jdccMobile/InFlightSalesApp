package com.jdmobile.inflightsalesapp.ui.screens.product.model

import java.math.BigDecimal

data class CartSummary(
    val total: BigDecimal = BigDecimal.ZERO,
    val itemCount: Int = 0
)