package com.jdmobile.inflightsalesapp.di

import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.SyncProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.UpdateProductStockUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::GetProductsUseCase)
    factoryOf(::SyncProductsUseCase)
    factoryOf(::UpdateProductStockUseCase)
}