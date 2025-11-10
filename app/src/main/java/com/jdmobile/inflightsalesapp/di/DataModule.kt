package com.jdmobile.inflightsalesapp.di

import androidx.room.Room
import com.jdmobile.inflightsalesapp.data.local.AppDatabase
import com.jdmobile.inflightsalesapp.data.local.product.ProductLocalDataSource
import com.jdmobile.inflightsalesapp.data.remote.RetrofitService
import com.jdmobile.inflightsalesapp.data.remote.RetrofitServiceFactory
import com.jdmobile.inflightsalesapp.data.remote.product.ProductRemoteDataSource
import com.jdmobile.inflightsalesapp.data.repository.ProductRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single<RetrofitService> { RetrofitServiceFactory.createRetrofitService() }
    factoryOf(::ProductRepository)
    factoryOf(::ProductRemoteDataSource)

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            DATABASE_NAME,
        ).build()
    }
    single { get<AppDatabase>().getProductDao() }
    factoryOf(::ProductLocalDataSource)
}

private const val DATABASE_NAME = "products_database"