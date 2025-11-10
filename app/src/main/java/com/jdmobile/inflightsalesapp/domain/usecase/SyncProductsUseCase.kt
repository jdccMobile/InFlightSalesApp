package com.jdmobile.inflightsalesapp.domain.usecase

import arrow.core.Either
import com.jdmobile.inflightsalesapp.data.repository.ProductRepository

class SyncProductsUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(): Either<Throwable, Unit> {
        return repository.syncProducts()
    }
}