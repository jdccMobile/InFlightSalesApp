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
import kotlinx.serialization.json.Json

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
                            products = applyQuantitiesFromCart(uiProducts, it.selectedProducts)
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
                products = applyQuantitiesFromCart(filtered, it.selectedProducts)
            )
        }
    }

    fun onCurrencySelected(currency: Currency) {
        _uiState.update { state ->
            state.copy(
                selectedCurrency = currency,
                cartTotal = calculateTotal(
                    state.allProducts,
                    state.selectedProducts,
                    currency,
                    state.selectedCustomerType
                )
            )
        }
    }

    fun onCustomerTypeSelected(customerType: CustomerType) {
        _uiState.update { state ->
            state.copy(
                selectedCustomerType = customerType,
                cartTotal = calculateTotal(
                    state.allProducts,
                    state.selectedProducts,
                    state.selectedCurrency,
                    customerType
                )
            )
        }
    }

    fun onAddProduct(productId: ProductId) {
        _uiState.update { state ->
            val newCart = state.selectedProducts.toMutableMap()
            newCart[productId] = (newCart[productId] ?: 0) + 1

            val updatedProducts = applyQuantitiesFromCart(state.products, newCart)
            val total = calculateTotal(
                state.allProducts,
                newCart,
                state.selectedCurrency,
                state.selectedCustomerType
            )
            val itemCount = calculateItemCount(newCart)

            state.copy(
                selectedProducts = newCart,
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    fun onRemoveProduct(productId: ProductId) {
        _uiState.update { state ->
            val newCart = state.selectedProducts.toMutableMap()
            val currentQuantity = newCart[productId] ?: 0

            if (currentQuantity > 0) {
                if (currentQuantity == 1) {
                    newCart.remove(productId)
                } else {
                    newCart[productId] = currentQuantity - 1
                }
            }

            val updatedProducts = applyQuantitiesFromCart(state.products, newCart)
            val total = calculateTotal(
                state.allProducts,
                newCart,
                state.selectedCurrency,
                state.selectedCustomerType
            )
            val itemCount = calculateItemCount(newCart)

            state.copy(
                selectedProducts = newCart,
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    private fun applyQuantitiesFromCart(
        products: List<ProductUi>,
        cart: Map<ProductId, Int>
    ): List<ProductUi> {
        return products.map { product ->
            product.copy(quantity = cart[product.id] ?: 0)
        }
    }

    private fun calculateTotal(
        allProducts: List<ProductUi>,
        cart: Map<ProductId, Int>,
        currency: Currency,
        customerType: CustomerType
    ): Double {
        return cart.entries.sumOf { (productId, quantity) ->
            val product = allProducts.find { it.id == productId } ?: return@sumOf 0.0
            val price = when (currency) {
                Currency.USD -> product.priceUSD
                Currency.EUR -> product.priceEUR
                Currency.GBP -> product.priceGBP
            }
            price * quantity * customerType.discount
        }
    }

    private fun calculateItemCount(cart: Map<ProductId, Int>): Int =
        cart.values.sum()

    fun onPayClicked() {
        val state = uiState.value

        if (state.selectedProducts.isEmpty()) return

        screenActions.onNavToReceipt(
            Json.encodeToString(state.selectedProducts),
            state.selectedCurrency.name,
            state.selectedCustomerType.name,
        )
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
    val selectedProducts: Map<ProductId, Int> = emptyMap(),
    val cartTotal: Double = 0.0,
    val cartItemCount: Int = 0
)

data class CartItem(
    val productId: ProductId,
    val quantity: Int
)

data class ProductScreenActions(
    val onNavBack: () -> Unit,
    val onNavToReceipt: (selectedProducts: String, currency: String, customerType: String) -> Unit,
)