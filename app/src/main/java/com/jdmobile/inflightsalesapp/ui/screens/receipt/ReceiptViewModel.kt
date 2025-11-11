package com.jdmobile.inflightsalesapp.ui.screens.receipt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.screens.product.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CustomerType
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReceiptViewModel(
    private val screenActions: ReceiptScreenActions,
    private val receiptInitialData: ReceiptInitialData,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ReceiptUiState(
//            products = initialProducts,
//            selectedCurrency = initialCurrency,
//            total = initialTotal,
            selectedPaymentMethod = PaymentMethod.CASH
        )
    )
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    fun onNavBack(){
        screenActions.onNavBack()
    }

    fun onRemoveProduct(productId: ProductId) {
        _uiState.update { state ->
            val updatedProducts = state.products.filter { it.id != productId }
            val total = calculateTotal(updatedProducts, state.selectedCurrency)

            state.copy(
                products = updatedProducts,
                total = total
            )
        }
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod) {
        _uiState.update {
            it.copy(selectedPaymentMethod = paymentMethod)
        }
    }

    fun onSeatNumberChanged(seatNumber: String) {
        _uiState.update {
            it.copy(seatNumber = seatNumber)
        }
    }

    fun onCardNumberChanged(cardNumber: String) {
        _uiState.update {
            it.copy(cardNumber = cardNumber)
        }
    }

    fun onExpirationDateChanged(expirationDate: String) {
        _uiState.update {
            it.copy(expirationDate = expirationDate)
        }
    }

    fun onCvvChanged(cvv: String) {
        _uiState.update {
            it.copy(cvv = cvv)
        }
    }

    fun onCardholderNameChanged(cardholderName: String) {
        _uiState.update {
            it.copy(cardholderName = cardholderName)
        }
    }

    fun onCashAmountChanged(amount: String) {
        _uiState.update { state ->
            val cashAmount = amount.toDoubleOrNull() ?: 0.0
            val change = if (cashAmount >= state.total) cashAmount - state.total else 0.0

            state.copy(
                cashAmount = amount,
                change = change
            )
        }
    }

    fun onProcessPayment() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validate based on payment method
            val isValid = when (state.selectedPaymentMethod) {
                PaymentMethod.CASH -> {
                    val cashAmount = state.cashAmount.toDoubleOrNull() ?: 0.0
                    cashAmount >= state.total
                }
                PaymentMethod.CARD -> {
                    state.cardNumber.isNotBlank() &&
                            state.expirationDate.isNotBlank() &&
                            state.cvv.isNotBlank() &&
                            state.cardholderName.isNotBlank()
                }
            }

            if (isValid) {
                // Process payment logic here
            } else {
                _uiState.update {
                    it.copy(showValidationError = true)
                }
            }
        }
    }

    private fun calculateTotal(products: List<ProductUi>, currency: Currency): Double {
        return products.sumOf { product ->
            val price = when (currency) {
                Currency.USD -> product.priceUSD
                Currency.EUR -> product.priceEUR
                Currency.GBP -> product.priceGBP
            }
            price * product.quantity
        }
    }
}

data class ReceiptUiState(
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
    val change: Double = 0.0,
    val showValidationError: Boolean = false
)

data class ReceiptScreenActions(
    val onNavBack: () -> Unit,
)

enum class PaymentMethod {
    CASH,
    CARD
}

data class ReceiptInitialData(
    val selectedProducts: Map<ProductId, Int>,
    val currency: Currency,
    val customerType: CustomerType,
)