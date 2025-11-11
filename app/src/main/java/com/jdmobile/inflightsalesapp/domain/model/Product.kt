package com.jdmobile.inflightsalesapp.domain.model

import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import kotlinx.serialization.Serializable

data class Product(
    val id: ProductId,
    val name: String,
    val stock: Int,
    val priceUSD: Double,
    val priceEUR: Double,
    val priceGBP: Double,
    val imageUrl: String,
    val category: ProductFilter
)

@Serializable
@JvmInline
value class ProductId(val value: Int)