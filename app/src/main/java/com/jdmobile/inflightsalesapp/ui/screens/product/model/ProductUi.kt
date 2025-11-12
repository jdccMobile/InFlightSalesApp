package com.jdmobile.inflightsalesapp.ui.screens.product.model

import com.jdmobile.inflightsalesapp.domain.model.Product
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import java.math.BigDecimal

data class ProductUi(
    val id: ProductId,
    val name: String,
    val stock: Int,
    val priceUSD: BigDecimal,
    val priceEUR: BigDecimal,
    val priceGBP: BigDecimal,
    val finalPrice: BigDecimal,
    val imageUrl: String,
    val unitsSelected: Int = 0,
    val category: ProductFilter = ProductFilter.ALL
)

fun Product.toUi(finalPrice: BigDecimal = BigDecimal.ZERO) = ProductUi(
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