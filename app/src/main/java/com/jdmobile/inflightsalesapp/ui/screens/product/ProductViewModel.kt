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
import kotlinx.serialization.Serializable
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
        if(!ProductSyncState.isSynced) syncProducts()
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
                    val discountedProducts = applyDiscountedPrices(
                        uiProducts,
                        _uiState.value.selectedCurrency,
                        _uiState.value.selectedCustomerType
                    )
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            allProducts = discountedProducts,
                            products = applyQuantitiesFromCart(discountedProducts, it.selectedProducts)
                        )
                    }
                }
        }
    }

    private fun syncProducts() {
        viewModelScope.launch {
            syncProductsUseCase()
                .fold(
                    ifLeft = {
                        Log.e("ProductViewModel", "Error syncing products")
                    },
                    ifRight = {
                        ProductSyncState.isSynced = true
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
            val updatedProducts = applyDiscountedPrices(
                state.allProducts,
                currency,
                state.selectedCustomerType
            )
            state.copy(
                selectedCurrency = currency,
                allProducts = updatedProducts,
                products = applyQuantitiesFromCart(updatedProducts, state.selectedProducts),
                cartTotal = calculateTotal(
                    updatedProducts,
                    state.selectedProducts,
                    currency,
                    state.selectedCustomerType
                )
            )
        }
    }

    fun onCustomerTypeSelected(customerType: CustomerType) {
        _uiState.update { state ->
            val updatedProducts = applyDiscountedPrices(
                state.allProducts,
                state.selectedCurrency,
                customerType
            )
            state.copy(
                selectedCustomerType = customerType,
                allProducts = updatedProducts,
                products = applyQuantitiesFromCart(updatedProducts, state.selectedProducts),
                cartTotal = calculateTotal(
                    updatedProducts,
                    state.selectedProducts,
                    state.selectedCurrency,
                    customerType
                )
            )
        }
    }

    fun onAddProduct(productId: ProductId) {
        _uiState.update { state ->
            val existing = state.selectedProducts.toMutableList()
            val index = existing.indexOfFirst { it.productId == productId }

            val product = state.allProducts.find { it.id == productId } ?: return@update state
            val price = product.finalPrice

            if (index >= 0) {
                val current = existing[index]
                val newQuantity = current.quantity + 1
                existing[index] = current.copy(
                    quantity = newQuantity,
                    totalPrice = (price * newQuantity).toFloat()
                )
            } else {
                existing.add(
                    SelectedProductsUi(
                        productId = productId,
                        quantity = 1,
                        totalPrice = price.toFloat()
                    )
                )
            }

            val updatedProducts = applyQuantitiesFromCart(state.products, existing)
            val total = calculateTotal(
                state.allProducts,
                existing,
                state.selectedCurrency,
                state.selectedCustomerType
            )
            val itemCount = calculateItemCount(existing)

            state.copy(
                selectedProducts = existing,
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    fun onRemoveProduct(productId: ProductId) {
        _uiState.update { state ->
            val existing = state.selectedProducts.toMutableList()
            val index = existing.indexOfFirst { it.productId == productId }

            if (index >= 0) {
                val current = existing[index]
                if (current.quantity > 1) {
                    val product = state.allProducts.find { it.id == productId } ?: return@update state
                    val price = product.finalPrice
                    val newQuantity = current.quantity - 1

                    existing[index] = current.copy(
                        quantity = newQuantity,
                        totalPrice = (price * newQuantity).toFloat()
                    )
                } else {
                    existing.removeAt(index)
                }
            }

            val updatedProducts = applyQuantitiesFromCart(state.products, existing)
            val total = calculateTotal(
                state.allProducts,
                existing,
                state.selectedCurrency,
                state.selectedCustomerType
            )
            val itemCount = calculateItemCount(existing)

            state.copy(
                selectedProducts = existing,
                products = updatedProducts,
                cartTotal = total,
                cartItemCount = itemCount
            )
        }
    }

    private fun applyQuantitiesFromCart(
        products: List<ProductUi>,
        selectedProducts: List<SelectedProductsUi>
    ): List<ProductUi> {
        return products.map { product ->
            val selected = selectedProducts.find { it.productId == product.id }
            product.copy(unitsSelected = selected?.quantity ?: 0)
        }
    }

    private fun calculateTotal(
        allProducts: List<ProductUi>,
        selectedProducts: List<SelectedProductsUi>,
        currency: Currency,
        customerType: CustomerType
    ): Double {
        return selectedProducts.sumOf { selected ->
            val product = allProducts.find { it.id == selected.productId } ?: return@sumOf 0.0
            getProductPrice(product, currency, customerType) * selected.quantity
        }
    }

    private fun getProductPrice(
        product: ProductUi,
        currency: Currency,
        customerType: CustomerType
    ): Double {
        val basePrice = when (currency) {
            Currency.USD -> product.priceUSD
            Currency.EUR -> product.priceEUR
            Currency.GBP -> product.priceGBP
        }
        return basePrice * customerType.discount
    }

    private fun calculateItemCount(selectedProducts: List<SelectedProductsUi>): Int =
        selectedProducts.sumOf { it.quantity }

    fun onPayClicked() {
        val state = uiState.value

        screenActions.onNavToReceipt(
            Json.encodeToString(state.selectedProducts),
            state.selectedCurrency.name,
        )
    }

    private fun applyDiscountedPrices(
        products: List<ProductUi>,
        currency: Currency,
        customerType: CustomerType
    ): List<ProductUi> {
        return products.map { product ->
            val basePrice = when (currency) {
                Currency.USD -> product.priceUSD
                Currency.EUR -> product.priceEUR
                Currency.GBP -> product.priceGBP
            }
            val discountedPrice = basePrice * customerType.discount
            product.copy(finalPrice = discountedPrice)
        }
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
    val selectedProducts: List<SelectedProductsUi> = emptyList(),
    val cartTotal: Double = 0.0,
    val cartItemCount: Int = 0
)

@Serializable
data class SelectedProductsUi(
    val productId: ProductId,
    val quantity: Int,
    val totalPrice: Float,
)

data class ProductScreenActions(
    val onNavBack: () -> Unit,
    val onNavToReceipt: (selectedProducts: String, currency: String) -> Unit,
)

object ProductSyncState {
    var isSynced = false
}