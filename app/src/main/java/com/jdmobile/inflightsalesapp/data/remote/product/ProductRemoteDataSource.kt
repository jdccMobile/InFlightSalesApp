package com.jdmobile.inflightsalesapp.data.remote.product

import arrow.core.Either
import com.jdmobile.inflightsalesapp.data.remote.RetrofitService

class ProductRemoteDataSource(private val api: RetrofitService) {

    suspend fun getProducts(): Either<Throwable, List<ProductResponse>> =
        Either.catch { api.getProducts() }.mapLeft { it }
}