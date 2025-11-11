package com.jdmobile.inflightsalesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jdmobile.inflightsalesapp.ui.screens.product.ProductDestination
import com.jdmobile.inflightsalesapp.ui.screens.receipt.ReceiptDestination

@Composable
fun NavHost() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Route.Product,
    ) {
        composable<Route.Product> {
            ProductDestination(
                onNavBack = { navController.popBackStack() },
                onNavToReceipt = { selectedProducts, currency ->
                    navController.navigate(
                        Route.Receipt(
                            selectedProducts = selectedProducts,
                            currency = currency,
                        )
                    )
                },
            )
        }

        composable<Route.Receipt> { backStackEntry ->
            val receiptParams = backStackEntry.toRoute<Route.Receipt>()
            ReceiptDestination(
                onNavBack = { navController.popBackStack() },
                onNavToProducts = { navController.navigate(Route.Product) },
                selectedProducts = receiptParams.selectedProducts,
                currency = receiptParams.currency,
            )
        }
    }
}
