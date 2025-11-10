package com.jdmobile.inflightsalesapp.domain.usecase

import com.jdmobile.inflightsalesapp.data.repository.ProductRepository
import com.jdmobile.inflightsalesapp.domain.model.Product
import kotlinx.coroutines.flow.Flow

class GetProductsUseCase(private val repository: ProductRepository) {
    operator fun invoke(): Flow<List<Product>> {
        return repository.getProducts()
    }
}