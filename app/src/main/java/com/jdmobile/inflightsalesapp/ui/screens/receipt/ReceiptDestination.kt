@file:OptIn(ExperimentalMaterial3Api::class)

package com.jdmobile.inflightsalesapp.ui.screens.receipt

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jdmobile.inflightsalesapp.R
import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.service.PaymentValidator
import com.jdmobile.inflightsalesapp.ui.formatters.formatPrice
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CartItem
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import com.jdmobile.inflightsalesapp.ui.screens.receipt.model.ReceiptInitialData
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal
import kotlin.math.roundToInt

@Composable
fun ReceiptDestination(
    onNavigateBack: () -> Unit,
    onNavigateToProducts: () -> Unit,
    cart: String,
    currency: String
) {
    val initialData = ReceiptInitialData(
        cart = Json.decodeFromString<List<CartItem>>(cart),
        currency = Currency.valueOf(currency)
    )

    val screenActions = ReceiptScreenActions(
        onNavBack = onNavigateBack,
        onNavToProducts = onNavigateToProducts,
    )

    val viewModel: ReceiptViewModel = koinViewModel {
        parametersOf(screenActions, initialData)
    }

    ReceiptScreen(viewModel = viewModel)
}

@Composable
private fun ReceiptScreen(
    viewModel: ReceiptViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ReceiptContent(
        state = state,
        onNavigateBack = viewModel::onNavigateBack,
        onProductRemoved = viewModel::onProductRemoved,
        onSeatNumberChanged = viewModel::onSeatNumberChanged,
        onCashPaymentClicked = viewModel::onCashPaymentClicked,
        onCardPaymentClicked = viewModel::onCardPaymentClicked,
        onCashDialogDismissed = viewModel::onCashDialogDismissed,
        onCashAmountChanged = viewModel::onCashAmountChanged,
        onCashPaymentProcessed = viewModel::onCashPaymentProcessed,
        onCardDialogDismissed = viewModel::onCardDialogDismissed,
        onCardNumberChanged = viewModel::onCardNumberChanged,
        onExpirationDateChanged = viewModel::onExpirationDateChanged,
        onCvvChanged = viewModel::onCvvChanged,
        onCardholderNameChanged = viewModel::onCardholderNameChanged,
        onCardPaymentProcessed = viewModel::onCardPaymentProcessed,
        onSuccessDialogDismissed = viewModel::onSuccessDialogDismissed
    )
}

@Composable
private fun ReceiptContent(
    state: ReceiptUiState,
    onNavigateBack: () -> Unit,
    onProductRemoved: (ProductId) -> Unit,
    onSeatNumberChanged: (String) -> Unit,
    onCashPaymentClicked: () -> Unit,
    onCardPaymentClicked: () -> Unit,
    onCashDialogDismissed: () -> Unit,
    onCashAmountChanged: (String) -> Unit,
    onCashPaymentProcessed: () -> Unit,
    onCardDialogDismissed: () -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCardholderNameChanged: (String) -> Unit,
    onCardPaymentProcessed: () -> Unit,
    onSuccessDialogDismissed: () -> Unit
) {
    Scaffold(
        topBar = {
            ReceiptTopBar(onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            PaymentBottomBar(
                state = state,
                onSeatNumberChanged = onSeatNumberChanged,
                onCashPaymentClicked = onCashPaymentClicked,
                onCardPaymentClicked = onCardPaymentClicked
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.products.isEmpty() -> {
                    EmptyReceiptState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }

                else -> {
                    ProductList(
                        products = state.products,
                        currency = state.selectedCurrency,
                        onProductRemoved = onProductRemoved,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }
            }

            if (state.isProcessingPayment) {
                ProcessingPaymentOverlay()
            }
        }
    }

    if (state.showCashDialog) {
        CashPaymentDialog(
            state = state,
            onCashAmountChanged = onCashAmountChanged,
            onDismiss = onCashDialogDismissed,
            onConfirm = onCashPaymentProcessed
        )
    }

    if (state.showCardDialog) {
        CardPaymentDialog(
            state = state,
            onCardNumberChanged = onCardNumberChanged,
            onExpirationDateChanged = onExpirationDateChanged,
            onCvvChanged = onCvvChanged,
            onCardholderNameChanged = onCardholderNameChanged,
            onDismiss = onCardDialogDismissed,
            onConfirm = onCardPaymentProcessed
        )
    }

    if (state.showSuccessDialog) {
        PaymentSuccessDialog(onDismiss = onSuccessDialogDismissed)
    }
}

@Composable
private fun ReceiptTopBar(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.receipt),
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun EmptyReceiptState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Inventory2,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.6f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.no_products_added),
            color = Color.Gray.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProductList(
    products: List<ProductUi>,
    currency: Currency,
    onProductRemoved: (ProductId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = products,
            key = { it.id.value }
        ) { product ->
            SwipeableProductItem(
                product = product,
                currency = currency,
                onRemove = { onProductRemoved(product.id) }
            )
        }
    }
}

@Composable
private fun SwipeableProductItem(
    product: ProductUi,
    currency: Currency,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 80.dp
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "swipe_animation"
    )

    val swipeThreshold = -150f

    Box(modifier = modifier.fillMaxWidth()) {
        DeleteBackground(
            cardHeight = cardHeight,
            modifier = Modifier.fillMaxWidth()
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(product.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < swipeThreshold) {
                                onRemove()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                        }
                    )
                },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            ProductItemContent(
                product = product,
                currency = currency
            )
        }
    }
}

@Composable
private fun DeleteBackground(
    cardHeight: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(cardHeight)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE53935)),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier.padding(end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color.White
            )
            Text(
                text = stringResource(R.string.delete),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ProductItemContent(
    product: ProductUi,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            ProductImage(
                imageUrl = product.imageUrl,
                productName = product.name
            )

            ProductInfo(
                name = product.name,
                price = product.finalPrice,
                currency = currency
            )
        }

        ProductQuantity(quantity = product.unitsSelected)
    }
}

@Composable
private fun ProductImage(
    imageUrl: String,
    productName: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_no_image),
        error = painterResource(R.drawable.ic_no_image),
        contentDescription = productName,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun ProductInfo(
    name: String,
    price: BigDecimal,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = currency.formatPrice(price),
            fontSize = 13.sp,
            color = Color.Gray,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProductQuantity(
    quantity: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = quantity.toString(),
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
private fun ProcessingPaymentOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = stringResource(R.string.processing_payment),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun PaymentBottomBar(
    state: ReceiptUiState,
    onSeatNumberChanged: (String) -> Unit,
    onCashPaymentClicked: () -> Unit,
    onCardPaymentClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            SeatSelector(
                selectedSeat = state.seatNumber,
                onSeatSelected = onSeatNumberChanged
            )

            TotalDisplay(
                total = state.total,
                currency = state.selectedCurrency
            )
        }

        Spacer(Modifier.height(12.dp))

        PaymentMethodButtons(
            onCashClicked = onCashPaymentClicked,
            onCardClicked = onCardPaymentClicked,
            isEnabled = state.seatNumber.isNotBlank() && state.products.isNotEmpty()
        )
    }
}

@Composable
private fun SeatSelector(
    selectedSeat: String,
    onSeatSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val seats = remember {
        listOf("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3")
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.seat).uppercase(),
            color = Color.Gray,
            fontSize = 12.sp
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Button(
                onClick = { },
                modifier = Modifier.menuAnchor(PrimaryNotEditable),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = selectedSeat.ifBlank { stringResource(R.string.select) },
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                seats.forEach { seat ->
                    DropdownMenuItem(
                        text = { Text(seat) },
                        onClick = {
                            onSeatSelected(seat)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalDisplay(
    total: BigDecimal,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.total).uppercase(),
            color = Color.Gray,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = currency.formatPrice(total),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun PaymentMethodButtons(
    onCashClicked: () -> Unit,
    onCardClicked: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PaymentMethodButton(
            text = stringResource(R.string.cash),
            icon = Icons.Default.Payments,
            backgroundColor = Color.Black,
            onClick = onCashClicked,
            enabled = isEnabled,
            modifier = Modifier.weight(1f)
        )

        PaymentMethodButton(
            text = stringResource(R.string.card),
            icon = Icons.Default.CreditCard,
            backgroundColor = Color.Blue,
            onClick = onCardClicked,
            enabled = isEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PaymentMethodButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
            Text(
                text = text,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CashPaymentDialog(
    state: ReceiptUiState,
    onCashAmountChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val validator = remember { PaymentValidator() }
    val change = remember(state.cashAmount, state.total) {
        validator.calculateChange(state.cashAmount, state.total)
    }
    val hasEnoughMoney = change >= BigDecimal.ZERO
    val showChange = change > BigDecimal.ZERO

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.cash_payment),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                TotalCard(
                    total = state.total,
                    currency = state.selectedCurrency
                )

                CashAmountInput(
                    cashAmount = state.cashAmount,
                    total = state.total,
                    currency = state.selectedCurrency,
                    hasError = state.hasValidationError && !hasEnoughMoney,
                    onCashAmountChanged = onCashAmountChanged
                )

                if (state.cashAmount.isNotBlank() && hasEnoughMoney && showChange) {
                    ChangeCard(
                        change = change,
                        currency = state.selectedCurrency
                    )
                }

                if (state.hasValidationError && !hasEnoughMoney) {
                    Text(
                        text = stringResource(R.string.insufficient_cash),
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                DialogButtons(
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                    confirmButtonColor = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun TotalCard(
    total: BigDecimal,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.total_to_pay),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = currency.formatPrice(total),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
private fun CashAmountInput(
    cashAmount: String,
    total: BigDecimal,
    currency: Currency,
    hasError: Boolean,
    onCashAmountChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = cashAmount,
        onValueChange = onCashAmountChanged,
        label = { Text(stringResource(R.string.cash_received)) },
        placeholder = { Text(String.format("%.2f", total)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Default.Payments, contentDescription = null)
        },
        suffix = { Text(currency.symbol) },
        isError = hasError
    )
}

@Composable
private fun ChangeCard(
    change: BigDecimal,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.change),
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = currency.formatPrice(change),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun CardPaymentDialog(
    state: ReceiptUiState,
    onCardNumberChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCardholderNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.card_payment),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                CardNumberInput(
                    cardNumber = state.cardData.number,
                    onCardNumberChanged = onCardNumberChanged
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExpirationDateInput(
                        expirationDate = state.cardData.expirationDate,
                        onExpirationDateChanged = onExpirationDateChanged,
                        modifier = Modifier.weight(1f)
                    )

                    CvvInput(
                        cvv = state.cardData.cvv,
                        onCvvChanged = onCvvChanged,
                        modifier = Modifier.weight(1f)
                    )
                }

                CardholderNameInput(
                    cardholderName = state.cardData.holderName,
                    onCardholderNameChanged = onCardholderNameChanged
                )

                if (state.hasValidationError) {
                    Text(
                        text = stringResource(R.string.complete_all_fields),
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                DialogButtons(
                    onDismiss = onDismiss,
                    onConfirm = onConfirm,
                    confirmButtonColor = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
private fun CardNumberInput(
    cardNumber: String,
    onCardNumberChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = cardNumber,
        onValueChange = onCardNumberChanged,
        label = { Text(stringResource(R.string.card_number)) },
        placeholder = { Text("1234 5678 9012 3456") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Default.CreditCard, contentDescription = null)
        }
    )
}

@Composable
private fun ExpirationDateInput(
    expirationDate: String,
    onExpirationDateChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = expirationDate,
        onValueChange = onExpirationDateChanged,
        label = { Text(stringResource(R.string.expiration)) },
        placeholder = { Text("MM/YY") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
        singleLine = true
    )
}

@Composable
private fun CvvInput(
    cvv: String,
    onCvvChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = cvv,
        onValueChange = onCvvChanged,
        label = { Text("CVV") },
        placeholder = { Text("123") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = modifier,
        singleLine = true
    )
}

@Composable
private fun CardholderNameInput(
    cardholderName: String,
    onCardholderNameChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = cardholderName,
        onValueChange = onCardholderNameChanged,
        label = { Text(stringResource(R.string.cardholder_name)) },
        placeholder = { Text("JOHN DOE") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null)
        }
    )
}

@Composable
private fun DialogButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmButtonColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.cancel))
        }

        Button(
            onClick = onConfirm,
            colors = ButtonDefaults.buttonColors(
                containerColor = confirmButtonColor
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.pay_without_price))
        }
    }
}

@Composable
private fun PaymentSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp)
                )

                Text(
                    text = stringResource(R.string.payment_successful),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.thank_you),
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.back_to_products),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
