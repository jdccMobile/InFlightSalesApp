package com.jdmobile.inflightsalesapp.ui.screens.product

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jdmobile.inflightsalesapp.ui.screens.payment.Greeting
import com.jdmobile.inflightsalesapp.ui.theme.InFlightSalesAppTheme

@Composable
fun ProductDestination() {
    Greeting("Product")
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    InFlightSalesAppTheme {
        Greeting("Android")
    }
}