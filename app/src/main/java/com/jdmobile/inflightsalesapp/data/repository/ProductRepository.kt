package com.jdmobile.inflightsalesapp.data.repository

import arrow.core.Either
import arrow.core.continuations.either
import com.jdmobile.inflightsalesapp.data.local.product.ProductEntity
import com.jdmobile.inflightsalesapp.data.local.product.ProductLocalDataSource
import com.jdmobile.inflightsalesapp.data.remote.product.ProductRemoteDataSource
import com.jdmobile.inflightsalesapp.data.remote.product.ProductResponse
import com.jdmobile.inflightsalesapp.domain.model.Product
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepository(
    private val remoteDataSource: ProductRemoteDataSource,
    private val localDataSource: ProductLocalDataSource,
) {

    fun getProducts(): Flow<List<Product>> {
        return localDataSource.getAllProducts()
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun syncProducts(): Either<Throwable, Unit> {
        return either {
            val productsResponse = remoteDataSource.getProducts().bind()
            val entities = productsResponse.map { it.toEntity() }
            localDataSource.insertProducts(entities).bind()
        }
    }
}


private fun ProductResponse.toEntity(): ProductEntity =
    ProductEntity(
        id = this.id,
        name = this.name,
        unit = this.unit,
        priceUSD = this.priceUSD,
        priceEUR = this.priceEUR,
        priceGBP = this.priceGBP,
        imageUrl = this.imageUrl,
        category = this.category
    )

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

private fun ProductEntity.toDomain(): Product = Product(
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
