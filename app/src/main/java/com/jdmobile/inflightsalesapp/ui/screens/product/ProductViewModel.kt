package com.jdmobile.inflightsalesapp.ui.screens.product

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductViewModel(
    private val screenActions: ProductScreenActions
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val mockProductUis = listOf(
        ProductUi(
            id = "1",
            name = "Chicken Sandwich",
            unit = "2 unit",
            priceUSD = 7.99,
            priceEUR = 7.35,
            priceGBP = 6.32,
            imageUrl = "https://www.carniceriademadrid.es/wp-content/uploads/2022/09/smash-burger-que-es.jpg",
            category = ProductFilter.FOOD
        ),
        ProductUi(
            id = "2",
            name = "Vegetarian Wrap",
            unit = "1 unit",
            priceUSD = 6.49,
            priceEUR = 5.97,
            priceGBP = 5.13,
            imageUrl = "",
            category = ProductFilter.FOOD
        ),
        ProductUi(
            id = "3",
            name = "Fruit Salad",
            unit = "1 unit",
            priceUSD = 4.99,
            priceEUR = 4.59,
            priceGBP = 3.95,
            imageUrl = "",
            category = ProductFilter.FOOD
        ),
        ProductUi(
            id = "4",
            name = "Cheese & Crackers",
            unit = "1 unit",
            priceUSD = 5.50,
            priceEUR = 5.06,
            priceGBP = 4.35,
            imageUrl = "",
            category = ProductFilter.FOOD
        ),
        ProductUi(
            id = "5",
            name = "Sparkling Water",
            unit = "6 unit",
            priceUSD = 3.99,
            priceEUR = 3.67,
            priceGBP = 3.16,
            imageUrl = "",
            category = ProductFilter.BEVERAGES
        ),
        ProductUi(
            id = "6",
            name = "Coffee",
            unit = "0 unit",
            priceUSD = 2.50,
            priceEUR = 2.30,
            priceGBP = 1.98,
            imageUrl = "",
            category = ProductFilter.BEVERAGES
        ),
        ProductUi(
            id = "7",
            name = "Sparkling Water",
            unit = "6 unit",
            priceUSD = 3.99,
            priceEUR = 3.67,
            priceGBP = 3.16,
            imageUrl = "",
            category = ProductFilter.BEVERAGES
        ),
        ProductUi(
            id = "8",
            name = "Coffee",
            unit = "0 unit",
            priceUSD = 2.50,
            priceEUR = 2.30,
            priceGBP = 1.98,
            imageUrl = "",
            category = ProductFilter.BEVERAGES
        )
    )

    private var allProducts = mockProductUis

    init {
        loadProducts()
    }

    private fun loadProducts() {
        _uiState.update {
            it.copy(
                isLoading = false,
                products = allProducts
            )
        }
    }

    fun onFilterSelected(filter: ProductFilter) {
        val filtered = when (filter) {
            ProductFilter.ALL -> allProducts
            else -> allProducts.filter { it.category == filter }
        }

        _uiState.update {
            it.copy(
                selectedFilter = filter,
                products = filtered
            )
        }
    }

    fun onCurrencySelected(currency: Currency) {
        _uiState.update { state ->
            state.copy(
                selectedCurrency = currency,
                cartTotal = calculateTotal(state.products, currency, state.selectedCustomerType)
            )
        }
    }

    fun onCustomerTypeSelected(customerType: CustomerType) {
        _uiState.update { state ->
            state.copy(
                selectedCustomerType = customerType,
                cartTotal = calculateTotal(state.products, state.selectedCurrency, customerType)
            )
        }
    }

    fun onAddProduct(productId: String) {
        _uiState.update { state ->
            val updatedProducts = state.products.map { product ->
                if (product.id == productId) product.copy(quantity = product.quantity + 1)
                else product
            }

            val total = calculateTotal(updatedProducts, state.selectedCurrency, state.selectedCustomerType)
            val itemCount = calculateItemCount(updatedProducts)

            state.copy(
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    fun onRemoveProduct(productId: String) {
        _uiState.update { state ->
            val updatedProducts = state.products.map { product ->
                if (product.id == productId && product.quantity > 0) {
                    product.copy(quantity = product.quantity - 1)
                } else product
            }

            val total = calculateTotal(updatedProducts, state.selectedCurrency, state.selectedCustomerType)
            val itemCount = calculateItemCount(updatedProducts)

            state.copy(
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    private fun calculateTotal(
        productUis: List<ProductUi>,
        currency: Currency,
        customerType: CustomerType
    ): Double {
        return productUis.sumOf { product ->
            val price = when (currency) {
                Currency.USD -> product.priceUSD
                Currency.EUR -> product.priceEUR
                Currency.GBP -> product.priceGBP
            }
            price * product.quantity * customerType.discount
        }
    }

    private fun calculateItemCount(productUis: List<ProductUi>): Int =
        productUis.sumOf { it.quantity }

    fun onPayClicked() {
        // TODO: Implementar lógica de pago
    }
}

data class ProductUiState(
    val isLoading: Boolean = true,
    val products: List<ProductUi> = emptyList(),
    val selectedFilter: ProductFilter = ProductFilter.ALL,
    val selectedCurrency: Currency = Currency.USD,
    val selectedCustomerType: CustomerType = CustomerType.RETAIL,
    val cartTotal: Double = 0.0,
    val cartItemCount: Int = 0
)

data class ProductScreenActions(
    val onNavBack: () -> Unit
)
