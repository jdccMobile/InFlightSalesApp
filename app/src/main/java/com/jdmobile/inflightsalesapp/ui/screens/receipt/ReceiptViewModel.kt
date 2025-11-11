package com.jdmobile.inflightsalesapp.ui.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.ui.screens.product.SelectedProductsUi
import com.jdmobile.inflightsalesapp.ui.screens.product.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import com.jdmobile.inflightsalesapp.ui.screens.product.model.toUi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiptViewModel(
    private val screenActions: ReceiptScreenActions,
    private val receiptInitialData: ReceiptInitialData,
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReceiptUiState(
            selectedCurrency = receiptInitialData.currency,
        )
    )
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            getProductsUseCase().collect { domainProducts ->
                val allProducts = domainProducts.map { it.toUi() }

                val selectedProducts = receiptInitialData.selectedProducts.mapNotNull { selected ->
                    allProducts.find { it.id == selected.productId }?.copy(
                        unitsSelected = selected.quantity,
                        finalPrice = selected.totalPrice.toDouble()
                    )
                }

                val total = selectedProducts.sumOf { it.finalPrice }

                _uiState.update {
                    it.copy(
                        products = selectedProducts,
                        total = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNavBack() {
        screenActions.onNavBack()
    }

    fun onRemoveProduct(productId: ProductId) {
        _uiState.update { state ->
            val updatedProducts = state.products.filter { it.id != productId }
            val total = calculateTotal(products = updatedProducts,)

            state.copy(
                products = updatedProducts,
                total = total
            )
        }
    }

    fun onSeatNumberChanged(seatNumber: String) {
        _uiState.update { it.copy(seatNumber = seatNumber) }
    }

    fun onCashPaymentClicked() {
        if (uiState.value.seatNumber.isNotBlank() && uiState.value.products.isNotEmpty()) {
            _uiState.update { it.copy(showCashDialog = true, showValidationError = false) }
        }
    }

    fun onDismissCashDialog() {
        _uiState.update { it.copy(showCashDialog = false, cashAmount = "") }
    }

    fun onCashAmountChanged(amount: String) {
        _uiState.update { it.copy(cashAmount = amount) }
    }

    fun onProcessCashPayment() {
        viewModelScope.launch {
            val cashAmount = _uiState.value.cashAmount.toDoubleOrNull() ?: 0.0

            if (cashAmount < _uiState.value.total) {
                _uiState.update { it.copy(showValidationError = true) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    showCashDialog = false,
                    isProcessingPayment = true,
                    showValidationError = false
                )
            }


            _uiState.update {
                it.copy(
                    isProcessingPayment = false,
                    showSuccessDialog = true
                )
            }
        }
    }

    fun onCardPaymentClicked() {
        if (uiState.value.seatNumber.isNotBlank() && uiState.value.products.isNotEmpty()) {
            _uiState.update { it.copy(showCardDialog = true) }
        }
    }

    fun onDismissCardDialog() {
        _uiState.update { it.copy(showCardDialog = false) }
    }

    fun onCardNumberChanged(cardNumber: String) {
        _uiState.update { it.copy(cardNumber = cardNumber) }
    }

    fun onExpirationDateChanged(expirationDate: String) {
        _uiState.update { it.copy(expirationDate = expirationDate) }
    }

    fun onCvvChanged(cvv: String) {
        _uiState.update { it.copy(cvv = cvv) }
    }

    fun onCardholderNameChanged(cardholderName: String) {
        _uiState.update { it.copy(cardholderName = cardholderName) }
    }

    fun onProcessCardPayment() {
        viewModelScope.launch {
            val state = _uiState.value

            val isValid = state.cardNumber.isNotBlank() &&
                    state.expirationDate.isNotBlank() &&
                    state.cvv.isNotBlank() &&
                    state.cardholderName.isNotBlank() &&
                    state.seatNumber.isNotBlank()

            if (!isValid) {
                _uiState.update { it.copy(showValidationError = true) }
                return@launch
            }

            _uiState.update {
                it.copy(
                    showCardDialog = false,
                    isProcessingPayment = true,
                    showValidationError = false
                )
            }

            delay(2000)

            _uiState.update {
                it.copy(
                    isProcessingPayment = false,
                    showSuccessDialog = true
                )
            }
        }
    }

    fun onDismissSuccessDialog() {
        _uiState.update { it.copy(showSuccessDialog = false) }
        screenActions.onNavToProducts()
    }

    private fun calculateTotal(
        products: List<ProductUi>,
    ): Double {
        return products.sumOf { product -> product.finalPrice }
    }
}

data class ReceiptUiState(
    val isLoading: Boolean = true,
    val products: List<ProductUi> = emptyList(),
    val selectedCurrency: Currency = Currency.USD,
    val total: Double = 0.0,
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.CASH,
    val seatNumber: String = "",
    val cardNumber: String = "",
    val expirationDate: String = "",
    val cvv: String = "",
    val cardholderName: String = "",
    val cashAmount: String = "",
    val showCardDialog: Boolean = false,
    val showCashDialog: Boolean = false,
    val isProcessingPayment: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val showValidationError: Boolean = false,
)

data class ReceiptScreenActions(
    val onNavBack: () -> Unit,
    val onNavToProducts: () -> Unit,
)

enum class PaymentMethod {
    CASH,
    CARD
}

data class ReceiptInitialData(
    val selectedProducts: List<SelectedProductsUi>,
    val currency: Currency,
)