package com.jdmobile.inflightsalesapp.data.local.product

import arrow.core.Either
import kotlinx.coroutines.flow.Flow

class ProductLocalDataSource(private val productDao: ProductDao) {

    fun getAllProducts(): Flow<List<ProductEntity>> {
        return productDao.getAllProducts()
    }

    fun getProductsByCategory(category: Int): Flow<List<ProductEntity>> {
        return productDao.getProductsByCategory(category)
    }

    suspend fun insertProducts(products: List<ProductEntity>): Either<Throwable, Unit> {
        return Either.catch { productDao.insertProducts(products) }
    }

    suspend fun updateProductStock(id: Int, newStock: Int): Either<Throwable, Unit> {
        return Either.catch { productDao.updateProductStock(id, newStock) }
    }

    suspend fun deleteAllProducts(): Either<Throwable, Unit> {
        return Either.catch { productDao.deleteAllProducts() }
    }
}
