package com.jdmobile.inflightsalesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jdmobile.inflightsalesapp.ui.screens.payment.PaymentDestination
import com.jdmobile.inflightsalesapp.ui.screens.product.ProductDestination

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
            )
        }
        composable<Route.Payment> {
            PaymentDestination()
        }
    }
}
