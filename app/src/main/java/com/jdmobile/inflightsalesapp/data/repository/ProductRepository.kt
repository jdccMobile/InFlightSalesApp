package com.jdmobile.inflightsalesapp.data.repository

import arrow.core.Either
import com.jdmobile.inflightsalesapp.data.remote.datasource.ProductRemoteDataSource
import com.jdmobile.inflightsalesapp.data.remote.model.ProductResponse
import com.jdmobile.inflightsalesapp.domain.model.Product
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter

class ProductRepository(
    private val productRemoteDataSource: ProductRemoteDataSource,
) {
    suspend fun getProducts(): Either<Throwable, List<Product>> =
        productRemoteDataSource.getProducts().map { products -> products.map { it.toDomain() } }
}

private fun ProductResponse.toDomain(): Product = Product(
    id = ProductId(this.id),
    name = this.name,
    unit = this.unit,
    priceUSD = this.priceUSD,
    priceEUR = this.priceEUR,
    priceGBP = this.priceGBP,
    imageUrl = this.imageUrl,
    category = when (this.category) {
        1 -> ProductFilter.FOOD
        2 -> ProductFilter.BEVERAGES
        else -> ProductFilter.FOOD
    }
)