package com.jdmobile.inflightsalesapp.ui.screens.product

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.domain.model.CustomerType
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.service.PriceCalculator
import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.SyncProductsUseCase
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CartItem
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CartSummary
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
import java.math.BigDecimal

class ProductViewModel(
    private val screenActions: ProductScreenActions,
    private val getProductsUseCase: GetProductsUseCase,
    private val syncProductsUseCase: SyncProductsUseCase,
    private val priceCalculator: PriceCalculator = PriceCalculator()
) : ViewModel() {

    private val _state = MutableStateFlow(ProductUiState())
    val state: StateFlow<ProductUiState> = _state.asStateFlow()

    init {
        loadProducts()
        syncProductsIfNeeded()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase()
                .catch { handleError(it) }
                .collect { products -> handleProductsLoaded(products) }
        }
    }

    private fun handleError(error: Throwable) {
        Log.e(TAG, "Error loading products", error)
        _state.update { it.copy(isLoading = false, hasError = true) }
    }

    private fun handleProductsLoaded(products: List<com.jdmobile.inflightsalesapp.domain.model.Product>) {
        val uiProducts = products.map { it.toUi() }
        val productsWithPrices = applyPricingToProducts(uiProducts)

        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                allProducts = productsWithPrices,
                filteredProducts = applyCart(productsWithPrices, currentState.cart)
            )
        }
    }

    private fun syncProductsIfNeeded() {
        if (ProductSyncState.isSynced) return

        viewModelScope.launch {
            syncProductsUseCase()
                .fold(
                    ifLeft = { Log.e(TAG, "Failed to sync products") },
                    ifRight = {
                        ProductSyncState.isSynced = true
                        Log.d(TAG, "Products synced successfully")
                    }
                )
        }
    }

    fun onFilterSelected(filter: ProductFilter) {
        val filtered = when (filter) {
            ProductFilter.ALL -> _state.value.allProducts
            else -> _state.value.allProducts.filter { it.category == filter }
        }

        _state.update { currentState ->
            currentState.copy(
                selectedFilter = filter,
                filteredProducts = applyCart(filtered, currentState.cart)
            )
        }
    }

    fun onCurrencyChanged(currency: Currency) {
        _state.update { currentState ->
            val updatedProducts = applyPricingToProducts(
                products = currentState.allProducts,
                currency = currency,
                customerType = currentState.selectedCustomerType
            )
            val newCart = recalculateCart(currentState.cart, updatedProducts)

            val filtered = when (currentState.selectedFilter) {
                ProductFilter.ALL -> updatedProducts
                else -> updatedProducts.filter { it.category == currentState.selectedFilter }
            }

            currentState.copy(
                selectedCurrency = currency,
                allProducts = updatedProducts,
                filteredProducts = applyCart(filtered, newCart),
                cart = newCart,
                cartSummary = calculateCartSummary(newCart, updatedProducts)
            )
        }
    }


    fun onCustomerTypeChanged(customerType: CustomerType) {
        _state.update { currentState ->
            val updatedProducts = applyPricingToProducts(
                products = currentState.allProducts,
                currency = currentState.selectedCurrency,
                customerType = customerType
            )

            val newCart = recalculateCart(currentState.cart, updatedProducts)

            val filtered = when (currentState.selectedFilter) {
                ProductFilter.ALL -> updatedProducts
                else -> updatedProducts.filter { it.category == currentState.selectedFilter }
            }

            currentState.copy(
                selectedCustomerType = customerType,
                allProducts = updatedProducts,
                filteredProducts = applyCart(filtered, newCart),
                cart = newCart,
                cartSummary = calculateCartSummary(newCart, updatedProducts)
            )
        }
    }

    fun onProductAdded(productId: ProductId) {
        _state.update { currentState ->
            val product = currentState.allProducts.find { it.id == productId }
                ?: return@update currentState

            val updatedCart = addToCart(currentState.cart, product)
            val updatedProducts = applyCart(currentState.filteredProducts, updatedCart)

            currentState.copy(
                cart = updatedCart,
                filteredProducts = updatedProducts,
                cartSummary = calculateCartSummary(updatedCart, currentState.allProducts)
            )
        }
    }

    fun onProductRemoved(productId: ProductId) {
        _state.update { currentState ->
            val product = currentState.allProducts.find { it.id == productId }
                ?: return@update currentState

            val updatedCart = removeFromCart(currentState.cart, product)
            val updatedProducts = applyCart(currentState.filteredProducts, updatedCart)

            currentState.copy(
                cart = updatedCart,
                filteredProducts = updatedProducts,
                cartSummary = calculateCartSummary(updatedCart, currentState.allProducts)
            )
        }
    }

    fun onPayClicked() {
        val currentState = _state.value
        screenActions.onNavToReceipt(
            Json.encodeToString(currentState.cart),
            currentState.selectedCurrency.name
        )
    }


    private fun applyPricingToProducts(
        products: List<ProductUi>,
        currency: Currency = _state.value.selectedCurrency,
        customerType: CustomerType = _state.value.selectedCustomerType
    ): List<ProductUi> {
        return products.map { product ->
            val finalPrice = priceCalculator.calculateFinalPrice(
                product = product,
                currency = currency,
                customerType = customerType
            )
            product.copy(finalPrice = finalPrice)
        }
    }


    private fun addToCart(
        cart: List<CartItem>,
        product: ProductUi
    ): List<CartItem> {
        val mutableCart = cart.toMutableList()
        val existingIndex = mutableCart.indexOfFirst { it.productId == product.id }

        if (existingIndex >= 0) {
            val existing = mutableCart[existingIndex]
            val newQuantity = existing.quantity + 1
            mutableCart[existingIndex] = existing.copy(
                quantity = newQuantity,
                totalPrice = product.finalPrice.multiply(BigDecimal.valueOf(newQuantity.toLong()))
            )
        } else {
            mutableCart.add(
                CartItem(
                    productId = product.id,
                    quantity = 1,
                    totalPrice = product.finalPrice
                )
            )
        }

        return mutableCart
    }

    private fun removeFromCart(
        cart: List<CartItem>,
        product: ProductUi
    ): List<CartItem> {
        val mutableCart = cart.toMutableList()
        val existingIndex = mutableCart.indexOfFirst { it.productId == product.id }

        if (existingIndex >= 0) {
            val existing = mutableCart[existingIndex]
            if (existing.quantity > 1) {
                val newQuantity = existing.quantity - 1
                mutableCart[existingIndex] = existing.copy(
                    quantity = newQuantity,
                    totalPrice = product.finalPrice.multiply(BigDecimal.valueOf(newQuantity.toLong()))
                )
            } else {
                mutableCart.removeAt(existingIndex)
            }
        }

        return mutableCart
    }

    private fun recalculateCart(
        cart: List<CartItem>,
        products: List<ProductUi>
    ): List<CartItem> {
        return cart.map { item ->
            val product = products.find { it.id == item.productId } ?: return@map item
            item.copy(totalPrice = product.finalPrice.multiply(BigDecimal.valueOf(item.quantity.toLong())))
        }
    }

    private fun applyCart(
        products: List<ProductUi>,
        cart: List<CartItem>
    ): List<ProductUi> {
        return products.map { product ->
            val cartItem = cart.find { it.productId == product.id }
            product.copy(unitsSelected = cartItem?.quantity ?: 0)
        }
    }

    private fun calculateCartSummary(
        cart: List<CartItem>,
        allProducts: List<ProductUi>
    ): CartSummary {
        val total = cart.fold(BigDecimal.ZERO) { acc, cartItem ->
            val product = allProducts.find { it.id == cartItem.productId }
            if (product != null) {
                acc + (product.finalPrice.multiply(BigDecimal.valueOf(cartItem.quantity.toLong())))
            } else {
                acc
            }
        }

        val itemCount = cart.sumOf { it.quantity }

        return CartSummary(
            total = total,
            itemCount = itemCount
        )
    }
}

data class ProductUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val allProducts: List<ProductUi> = emptyList(),
    val filteredProducts: List<ProductUi> = emptyList(),
    val selectedFilter: ProductFilter = ProductFilter.ALL,
    val selectedCurrency: Currency = Currency.USD,
    val selectedCustomerType: CustomerType = CustomerType.RETAIL,
    val cart: List<CartItem> = emptyList(),
    val cartSummary: CartSummary = CartSummary()
)

data class ProductScreenActions(
    val onNavToReceipt: (String, String) -> Unit,
)

object ProductSyncState {
    var isSynced = false
}

private const val TAG = "ProductViewModel"