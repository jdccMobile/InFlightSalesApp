package com.jdmobile.inflightsalesapp.di

import com.jdmobile.inflightsalesapp.data.remote.RetrofitService
import com.jdmobile.inflightsalesapp.data.remote.RetrofitServiceFactory
import com.jdmobile.inflightsalesapp.data.remote.datasource.ProductRemoteDataSource
import com.jdmobile.inflightsalesapp.data.repository.ProductRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single<RetrofitService> { RetrofitServiceFactory.createRetrofitService() }
    factoryOf(::ProductRepository)
    factoryOf(::ProductRemoteDataSource)
}