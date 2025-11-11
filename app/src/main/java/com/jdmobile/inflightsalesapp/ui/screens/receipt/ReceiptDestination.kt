package com.jdmobile.inflightsalesapp.ui.screens.receipt

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jdmobile.inflightsalesapp.R
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.screens.product.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CustomerType
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun ReceiptDestination(
    onNavBack: () -> Unit,
    selectedProducts: String,
    currency: String,
    customerType: String,
) {

    val selectedProducts = Json.decodeFromString<Map<ProductId, Int>>(selectedProducts)
    val currencyEnum = Currency.valueOf(currency)
    val customerTypeEnum = CustomerType.valueOf(customerType)

    val initialData = ReceiptInitialData(
        selectedProducts = selectedProducts,
        currency = currencyEnum,
        customerType = customerTypeEnum,
    )

    val screenActions = ReceiptScreenActions(
        onNavBack = onNavBack,
    )

    val viewModel: ReceiptViewModel = koinViewModel(
        parameters = {
            parametersOf(screenActions, initialData)
        }
    )

    ReceiptScreen(viewModel)
}

@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReceiptContent(
        uiState = uiState,
        onNavBack = viewModel::onNavBack,
        onRemoveProduct = viewModel::onRemoveProduct,
        onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
        onSeatNumberChanged = viewModel::onSeatNumberChanged,
        onCardNumberChanged = viewModel::onCardNumberChanged,
        onExpirationDateChanged = viewModel::onExpirationDateChanged,
        onCvvChanged = viewModel::onCvvChanged,
        onCardholderNameChanged = viewModel::onCardholderNameChanged,
        onCashAmountChanged = viewModel::onCashAmountChanged,
        onProcessPayment = viewModel::onProcessPayment
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptContent(
    uiState: ReceiptUiState,
    onNavBack: () -> Unit,
    onRemoveProduct: (ProductId) -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onSeatNumberChanged: (String) -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCardholderNameChanged: (String) -> Unit,
    onCashAmountChanged: (String) -> Unit,
    onProcessPayment: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.receipt),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.selected_products),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.products, key = { it.id.value }) { product ->
                    SwipeableProductItem(
                        product = product,
                        currency = uiState.selectedCurrency,
                        onRemove = { onRemoveProduct(product.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    TotalSection(
                        total = uiState.total,
                        currency = uiState.selectedCurrency
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SeatSelectionDropdown(
                        seatNumber = uiState.seatNumber,
                        onSeatNumberChanged = onSeatNumberChanged
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    PaymentMethodSelector(
                        selectedMethod = uiState.selectedPaymentMethod,
                        onMethodSelected = onPaymentMethodSelected
                    )
                }

                item {
                    when (uiState.selectedPaymentMethod) {
                        PaymentMethod.CASH -> {
                            CashPaymentSection(
                                total = uiState.total,
                                currency = uiState.selectedCurrency,
                                cashAmount = uiState.cashAmount,
                                change = uiState.change,
                                onCashAmountChanged = onCashAmountChanged
                            )
                        }
                        PaymentMethod.CARD -> {
                            CardPaymentSection(
                                cardNumber = uiState.cardNumber,
                                expirationDate = uiState.expirationDate,
                                cvv = uiState.cvv,
                                cardholderName = uiState.cardholderName,
                                onCardNumberChanged = onCardNumberChanged,
                                onExpirationDateChanged = onExpirationDateChanged,
                                onCvvChanged = onCvvChanged,
                                onCardholderNameChanged = onCardholderNameChanged
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            ProcessPaymentButton(
                onProcessPayment = onProcessPayment,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
            )
        }
    }
}

@Composable
private fun SwipeableProductItem(
    product: ProductUi,
    currency: Currency,
    onRemove: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = -200f

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < swipeThreshold) {
                                onRemove()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceAtMost(0f)
                        }
                    )
                },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(product.imageUrl)
                            .crossfade(true)
                            .build(),
                        placeholder = painterResource(R.drawable.ic_no_image),
                        error = painterResource(R.drawable.ic_no_image),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )

                    Column {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = product.getFormattedPrice(currency),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFC107), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = product.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalSection(
    total: Double,
    currency: Currency
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.total),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = String.format("%.2f %s", total, currency.symbol),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
private fun SeatSelectionDropdown(
    seatNumber: String,
    onSeatNumberChanged: (String) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.seat_selection),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = seatNumber,
            onValueChange = onSeatNumberChanged,
            label = { Text(stringResource(R.string.seat_number)) },
            placeholder = { Text("12A") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun PaymentMethodSelector(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.payment_method),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentMethodButton(
                text = stringResource(R.string.cash),
                icon = R.drawable.ic_no_image,
                isSelected = selectedMethod == PaymentMethod.CASH,
                onClick = { onMethodSelected(PaymentMethod.CASH) },
                modifier = Modifier.weight(1f)
            )

            PaymentMethodButton(
                text = stringResource(R.string.card),
                icon = R.drawable.ic_no_image,
                isSelected = selectedMethod == PaymentMethod.CARD,
                onClick = { onMethodSelected(PaymentMethod.CARD) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PaymentMethodButton(
    text: String,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF424242) else Color(0xFFE0E0E0),
            contentColor = if (isSelected) Color.White else Color.Black
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = text,
                modifier = Modifier.size(32.dp)
            )
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CashPaymentSection(
    total: Double,
    currency: Currency,
    cashAmount: String,
    change: Double,
    onCashAmountChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = cashAmount,
            onValueChange = onCashAmountChanged,
            label = { Text(stringResource(R.string.cash_amount)) },
            placeholder = { Text(String.format("%.2f", total)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            suffix = { Text(currency.symbol) }
        )

        if (change > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.change),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.2f %s", change, currency.symbol),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardPaymentSection(
    cardNumber: String,
    expirationDate: String,
    cvv: String,
    cardholderName: String,
    onCardNumberChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCardholderNameChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = cardNumber,
            onValueChange = onCardNumberChanged,
            label = { Text(stringResource(R.string.card_number)) },
            placeholder = { Text("1234 5678 9012 3456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = expirationDate,
                onValueChange = onExpirationDateChanged,
                label = { Text(stringResource(R.string.expiration_date)) },
                placeholder = { Text("MM/YY") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = cvv,
                onValueChange = onCvvChanged,
                label = { Text(stringResource(R.string.cvv)) },
                placeholder = { Text("123") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = cardholderName,
            onValueChange = onCardholderNameChanged,
            label = { Text(stringResource(R.string.cardholder_name)) },
            placeholder = { Text("JOHN DOE") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
private fun ProcessPaymentButton(
    onProcessPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onProcessPayment,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.process_payment),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}