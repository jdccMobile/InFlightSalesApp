package com.jdmobile.inflightsalesapp.di

import com.jdmobile.inflightsalesapp.ui.screens.product.ProductViewModel
import com.jdmobile.inflightsalesapp.ui.screens.receipt.ReceiptViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val uiModule = module {
    viewModelOf(::ProductViewModel)
    viewModelOf(::ReceiptViewModel)
}