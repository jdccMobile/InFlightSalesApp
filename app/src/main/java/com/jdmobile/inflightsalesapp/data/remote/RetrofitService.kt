package com.jdmobile.inflightsalesapp.data.remote

import com.jdmobile.inflightsalesapp.data.remote.product.ProductResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface RetrofitService {
    @GET("products")
    suspend fun getProducts(): List<ProductResponse>
}

object RetrofitServiceFactory {
    private const val BASE_URL = "https://my-json-server.typicode.com/jdccMobile/InFlightSalesApp/"
    fun createRetrofitService(): RetrofitService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitService::class.java)
    }
}