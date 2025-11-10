package com.jdmobile.inflightsalesapp.data.remote.datasource

import arrow.core.Either
import com.jdmobile.inflightsalesapp.data.remote.RetrofitService
import com.jdmobile.inflightsalesapp.data.remote.model.ProductResponse

class ProductRemoteDataSource(private val api: RetrofitService) {

    suspend fun getProducts(): Either<Throwable, List<ProductResponse>> =
        Either.catch { api.getProducts() }.mapLeft { it }
}

