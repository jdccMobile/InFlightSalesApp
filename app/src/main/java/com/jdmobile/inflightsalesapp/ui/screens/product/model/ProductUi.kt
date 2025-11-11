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
    val finalPrice: Double,
    val imageUrl: String,
    val unitsSelected: Int = 0,
    val category: ProductFilter = ProductFilter.ALL
)

fun Product.toUi(finalPrice: Double = 0.0) = ProductUi(
    id = id,
    name = name,
    stock = stock,
    priceUSD = priceUSD,
    priceEUR = priceEUR,
    priceGBP = priceGBP,
    imageUrl = imageUrl,
    category = category,
    finalPrice = finalPrice
)
