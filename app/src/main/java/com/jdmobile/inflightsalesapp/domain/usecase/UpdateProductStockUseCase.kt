package com.jdmobile.inflightsalesapp.domain.usecase

import com.jdmobile.inflightsalesapp.data.repository.ProductRepository
import com.jdmobile.inflightsalesapp.domain.model.ProductId

class UpdateProductStockUseCase(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(productId: ProductId, quantitySold: Int) {
        productRepository.updateProductStock(productId, quantitySold)
    }
}