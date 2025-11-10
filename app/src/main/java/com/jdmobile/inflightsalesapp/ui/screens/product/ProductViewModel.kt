package com.jdmobile.inflightsalesapp.ui.screens.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.SyncProductsUseCase
import com.jdmobile.inflightsalesapp.ui.screens.product.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CustomerType
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import com.jdmobile.inflightsalesapp.ui.screens.product.model.toUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    private val screenActions: ProductScreenActions,
    private val getProductsUseCase: GetProductsUseCase,
    private val syncProductsUseCase: SyncProductsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        syncProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase()
                .catch {
                    _uiState.update {
                        it.copy(isLoading = false, isThereError = true)
                    }
                }
                .collect { products ->
                    val uiProducts = products.map { it.toUi() }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allProducts = uiProducts,
                            products = uiProducts
                        )
                    }
                }
        }
    }

    private fun syncProducts() {
        viewModelScope.launch {
            syncProductsUseCase()
                .fold(
                    ifLeft = { error ->
                        Log.e("ProductViewModel", "Error syncing products")
                    },
                    ifRight = {
                        Log.d("ProductViewModel", "Products synced successfully")
                    }
                )
        }
    }

    fun onFilterSelected(filter: ProductFilter) {
        val filtered = when (filter) {
            ProductFilter.ALL -> uiState.value.allProducts
            else -> uiState.value.allProducts.filter { it.category == filter }
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

    fun onAddProduct(productId: ProductId) {
        _uiState.update { state ->
            val updatedProducts = state.products.map { product ->
                if (product.id == productId) product.copy(quantity = product.quantity + 1)
                else product
            }

            val total =
                calculateTotal(updatedProducts, state.selectedCurrency, state.selectedCustomerType)
            val itemCount = calculateItemCount(updatedProducts)

            state.copy(
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    fun onRemoveProduct(productId: ProductId) {
        _uiState.update { state ->
            val updatedProducts = state.products.map { product ->
                if (product.id == productId && product.quantity > 0) {
                    product.copy(quantity = product.quantity - 1)
                } else product
            }

            val total =
                calculateTotal(updatedProducts, state.selectedCurrency, state.selectedCustomerType)
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
    val isThereError: Boolean = false,
    val allProducts: List<ProductUi> = emptyList(),
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
