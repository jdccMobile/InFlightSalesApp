package com.jdmobile.inflightsalesapp.ui.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.service.PaymentValidator
import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.UpdateProductStockUseCase
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CartItem
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import com.jdmobile.inflightsalesapp.ui.screens.product.model.toUi
import com.jdmobile.inflightsalesapp.domain.model.CardData
import com.jdmobile.inflightsalesapp.ui.screens.receipt.model.ReceiptInitialData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class ReceiptViewModel(
    private val screenActions: ReceiptScreenActions,
    private val initialData: ReceiptInitialData,
    private val getProductsUseCase: GetProductsUseCase,
    private val updateProductStockUseCase: UpdateProductStockUseCase,
    private val paymentValidator: PaymentValidator = PaymentValidator()
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReceiptUiState(
            selectedCurrency = initialData.currency
        )
    )
    val state: StateFlow<ReceiptUiState> = _state.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase().collect { domainProducts ->
                val allProducts = domainProducts.map { it.toUi() }
                val receiptProducts = mapCartToReceiptProducts(
                    cart = initialData.cart,
                    allProducts = allProducts
                )

                _state.update {
                    it.copy(
                        products = receiptProducts,
                        total = calculateTotal(receiptProducts),
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun mapCartToReceiptProducts(
        cart: List<CartItem>,
        allProducts: List<ProductUi>
    ): List<ProductUi> {
        return cart.mapNotNull { cartItem ->
            allProducts.find { it.id == cartItem.productId }?.let { product ->
                val quantityBD = BigDecimal.valueOf(cartItem.quantity.toLong())
                val unitPrice = if (cartItem.quantity > 0) cartItem.totalPrice.divide(quantityBD) else BigDecimal.ZERO

                product.copy(
                    unitsSelected = cartItem.quantity,
                    finalPrice = unitPrice
                )
            }
        }
    }
    fun onNavigateBack() {
        screenActions.onNavBack()
    }

    fun onProductRemoved(productId: ProductId) {
        _state.update { currentState ->
            val updatedProducts = currentState.products.filter { it.id != productId }
            currentState.copy(
                products = updatedProducts,
                total = calculateTotal(updatedProducts)
            )
        }
    }

    fun onSeatNumberChanged(seatNumber: String) {
        _state.update { it.copy(seatNumber = seatNumber) }
    }

    fun onCashPaymentClicked() {
        val currentState = _state.value
        if (!canProcessPayment(currentState)) return

        _state.update {
            it.copy(
                showCashDialog = true,
                hasValidationError = false
            )
        }
    }

    fun onCashDialogDismissed() {
        _state.update {
            it.copy(
                showCashDialog = false,
                cashAmount = "",
                hasValidationError = false
            )
        }
    }

    fun onCashAmountChanged(amount: String) {
        _state.update { it.copy(cashAmount = amount) }
    }

    fun onCashPaymentProcessed() {
        val currentState = _state.value
        val validationResult = paymentValidator.validateCashPayment(
            cashAmount = currentState.cashAmount,
            total = currentState.total
        )

        if (!validationResult.isValid) {
            _state.update { it.copy(hasValidationError = true) }
            return
        }

        processCashPayment()
    }

    private fun processCashPayment() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showCashDialog = false,
                    isProcessingPayment = true,
                    hasValidationError = false
                )
            }

            delay(PAYMENT_PROCESSING_DELAY)
            updateProductStock()

            _state.update {
                it.copy(
                    isProcessingPayment = false,
                    showSuccessDialog = true
                )
            }
        }
    }

    fun onCardPaymentClicked() {
        val currentState = _state.value
        if (!canProcessPayment(currentState)) return

        _state.update {
            it.copy(
                showCardDialog = true,
                hasValidationError = false
            )
        }
    }

    fun onCardDialogDismissed() {
        _state.update {
            it.copy(
                showCardDialog = false,
                cardData = CardData(),
                hasValidationError = false
            )
        }
    }

    fun onCardNumberChanged(cardNumber: String) {
        _state.update {
            it.copy(cardData = it.cardData.copy(number = cardNumber))
        }
    }

    fun onExpirationDateChanged(expirationDate: String) {
        _state.update {
            it.copy(cardData = it.cardData.copy(expirationDate = expirationDate))
        }
    }

    fun onCvvChanged(cvv: String) {
        _state.update {
            it.copy(cardData = it.cardData.copy(cvv = cvv))
        }
    }

    fun onCardholderNameChanged(name: String) {
        _state.update {
            it.copy(cardData = it.cardData.copy(holderName = name))
        }
    }

    fun onCardPaymentProcessed() {
        val currentState = _state.value
        val validationResult = paymentValidator.validateCardPayment(
            cardData = currentState.cardData,
            hasSeatNumber = currentState.seatNumber.isNotBlank()
        )

        if (!validationResult.isValid) {
            _state.update { it.copy(hasValidationError = true) }
            return
        }

        processCardPayment()
    }

    private fun processCardPayment() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showCardDialog = false,
                    isProcessingPayment = true,
                    hasValidationError = false
                )
            }

            delay(PAYMENT_PROCESSING_DELAY)
            updateProductStock()

            _state.update {
                it.copy(
                    isProcessingPayment = false,
                    showSuccessDialog = true
                )
            }
        }
    }

    private fun updateProductStock() {
        viewModelScope.launch {
            val products = _state.value.products

            products.forEach { product ->
                updateProductStockUseCase(
                    productId = product.id,
                    quantitySold = product.unitsSelected
                )
            }
        }
    }

    fun onSuccessDialogDismissed() {
        _state.update { it.copy(showSuccessDialog = false) }
        screenActions.onNavToProducts()
    }

    private fun canProcessPayment(state: ReceiptUiState): Boolean {
        return state.seatNumber.isNotBlank() && state.products.isNotEmpty()
    }

    private fun calculateTotal(products: List<ProductUi>): BigDecimal {
        return products.fold(BigDecimal.ZERO) { acc, product ->
            acc + (product.finalPrice * BigDecimal.valueOf(product.unitsSelected.toLong()))
        }
    }

}

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val products: List<ProductUi> = emptyList(),
    val selectedCurrency: Currency = Currency.USD,
    val total: BigDecimal = BigDecimal.ZERO,
    val seatNumber: String = "",
    val cardData: CardData = CardData(),
    val cashAmount: String = "",
    val showCardDialog: Boolean = false,
    val showCashDialog: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val hasValidationError: Boolean = false
)

data class ReceiptScreenActions(
    val onNavBack: () -> Unit,
    val onNavToProducts: () -> Unit,
)

private const val PAYMENT_PROCESSING_DELAY = 2000L
