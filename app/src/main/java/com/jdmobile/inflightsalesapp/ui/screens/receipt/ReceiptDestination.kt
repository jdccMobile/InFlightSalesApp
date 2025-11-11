package com.jdmobile.inflightsalesapp.ui.screens.receipt

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.screens.product.SelectedProductsUi
import com.jdmobile.inflightsalesapp.ui.screens.product.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

@Composable
fun ReceiptDestination(
    onNavBack: () -> Unit,
    onNavToProducts: () -> Unit,
    selectedProducts: String,
    currency: String,
) {
    val selectedProducts = Json.decodeFromString<List<SelectedProductsUi>>(selectedProducts)
    val currencyEnum = Currency.valueOf(currency)

    val initialData = ReceiptInitialData(
        selectedProducts = selectedProducts,
        currency = currencyEnum,
    )

    val screenActions = ReceiptScreenActions(
        onNavBack = onNavBack,
        onNavToProducts = onNavToProducts,
    )

    val viewModel: ReceiptViewModel = koinViewModel(
        parameters = {
            parametersOf(screenActions, initialData)
        }
    )

    ReceiptScreen(viewModel)
}

@Composable
fun ReceiptScreen(viewModel: ReceiptViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReceiptContent(
        uiState = uiState,
        onNavBack = viewModel::onNavBack,
        onRemoveProduct = viewModel::onRemoveProduct,
        onSeatNumberChanged = viewModel::onSeatNumberChanged,
        onCashClicked = viewModel::onCashPaymentClicked,
        onCardClicked = viewModel::onCardPaymentClicked,
        onDismissCashDialog = viewModel::onDismissCashDialog,
        onCashAmountChanged = viewModel::onCashAmountChanged,
        onProcessCashPayment = viewModel::onProcessCashPayment,
        onDismissCardDialog = viewModel::onDismissCardDialog,
        onCardNumberChanged = viewModel::onCardNumberChanged,
        onExpirationDateChanged = viewModel::onExpirationDateChanged,
        onCvvChanged = viewModel::onCvvChanged,
        onCardholderNameChanged = viewModel::onCardholderNameChanged,
        onProcessCardPayment = viewModel::onProcessCardPayment,
        onDismissSuccessDialog = viewModel::onDismissSuccessDialog
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptContent(
    uiState: ReceiptUiState,
    onNavBack: () -> Unit,
    onRemoveProduct: (ProductId) -> Unit,
    onSeatNumberChanged: (String) -> Unit,
    onCashClicked: () -> Unit,
    onCardClicked: () -> Unit,
    onDismissCashDialog: () -> Unit,
    onCashAmountChanged: (String) -> Unit,
    onProcessCashPayment: () -> Unit,
    onDismissCardDialog: () -> Unit,
    onCardNumberChanged: (String) -> Unit,
    onExpirationDateChanged: (String) -> Unit,
    onCvvChanged: (String) -> Unit,
    onCardholderNameChanged: (String) -> Unit,
    onProcessCardPayment: () -> Unit,
    onDismissSuccessDialog: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receipt", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            PaymentFooter(
                seat = uiState.seatNumber,
                total = uiState.total,
                currency = uiState.selectedCurrency,
                onSeatSelected = onSeatNumberChanged,
                onCashClicked = onCashClicked,
                onCardClicked = onCardClicked
            )
        }
    ) { innerPadding ->
        if(!uiState.isLoading) {
            if (uiState.products.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
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
                        text = "No products added yet",
                        color = Color.Gray.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(uiState.products, key = { it.id.value }) { product ->
                        ReceiptProductItem(
                            product = product,
                            currency = uiState.selectedCurrency,
                            onRemoveItem = { onRemoveProduct(product.id) }
                        )
                    }
                }
            }
        }
        if (uiState.isProcessingPayment) {
            Box(
                modifier = Modifier
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
                            text = "Procesando pago...",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    if (uiState.showCashDialog) {
        CashPaymentDialog(
            total = uiState.total,
            currency = uiState.selectedCurrency,
            cashAmount = uiState.cashAmount,
            showError = uiState.showValidationError,
            onCashAmountChanged = onCashAmountChanged,
            onDismiss = onDismissCashDialog,
            onConfirm = onProcessCashPayment
        )
    }

    if (uiState.showCardDialog) {
        CardPaymentDialog(
            cardNumber = uiState.cardNumber,
            expirationDate = uiState.expirationDate,
            cvv = uiState.cvv,
            cardholderName = uiState.cardholderName,
            showError = uiState.showValidationError,
            onCardNumberChanged = onCardNumberChanged,
            onExpirationDateChanged = onExpirationDateChanged,
            onCvvChanged = onCvvChanged,
            onCardholderNameChanged = onCardholderNameChanged,
            onDismiss = onDismissCardDialog,
            onConfirm = onProcessCardPayment
        )
    }

    if (uiState.showSuccessDialog) {
        SuccessDialog(onDismiss = onDismissSuccessDialog)
    }
}

@Composable
fun CashPaymentDialog(
    total: Double,
    currency: Currency,
    cashAmount: String,
    showError: Boolean,
    onCashAmountChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val change = (cashAmount.toDoubleOrNull() ?: 0.0) - total
    val hasEnoughMoney = change >= 0

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
                    text = "Pago en efectivo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Card(
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
                            text = "Total a pagar:",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        Text(
                            text = String.format("%.2f %s", total, currency.symbol),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                OutlinedTextField(
                    value = cashAmount,
                    onValueChange = onCashAmountChanged,
                    label = { Text("Efectivo recibido") },
                    placeholder = { Text(String.format("%.2f", total)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Payments, contentDescription = null)
                    },
                    suffix = { Text(currency.symbol) },
                    isError = showError && !hasEnoughMoney
                )

                if (cashAmount.isNotBlank() && hasEnoughMoney && change > 0) {
                    Card(
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
                                text = "Cambio:",
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = String.format("%.2f %s", change, currency.symbol),
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                if (showError && !hasEnoughMoney) {
                    Text(
                        text = "El efectivo recibido debe ser mayor o igual al total",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pagar")
                    }
                }
            }
        }
    }
}

@Composable
fun CardPaymentDialog(
    cardNumber: String,
    expirationDate: String,
    cvv: String,
    cardholderName: String,
    showError: Boolean,
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
                    text = "Pago con tarjeta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = onCardNumberChanged,
                    label = { Text("Número de tarjeta") },
                    placeholder = { Text("1234 5678 9012 3456") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.CreditCard, contentDescription = null)
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = expirationDate,
                        onValueChange = onExpirationDateChanged,
                        label = { Text("Vencimiento") },
                        placeholder = { Text("MM/YY") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = cvv,
                        onValueChange = onCvvChanged,
                        label = { Text("CVV") },
                        placeholder = { Text("123") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = onCardholderNameChanged,
                    label = { Text("Titular de la tarjeta") },
                    placeholder = { Text("JOHN DOE") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                )

                if (showError) {
                    Text(
                        text = "Por favor completa todos los campos",
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pagar")
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
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
                    text = "¡Pago realizado correctamente!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Gracias por su compra",
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
                        text = "Volver a productos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptProductItem(
    product: ProductUi,
    currency: Currency,
    onRemoveItem: () -> Unit,
    cardHeight: Dp = 80.dp
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = -150f
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "")

    Box(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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
                    contentDescription = "Delete",
                    tint = Color.White
                )
                Text(
                    text = "Eliminar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .pointerInput(product.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < swipeThreshold) {
                                onRemoveItem()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val new = offsetX + dragAmount
                            offsetX = new.coerceAtMost(0f)
                        }
                    )
                },
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
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
                            .size(56.dp)
                            .clip(CircleShape)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = product.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val subtotal = when (currency) {
                            Currency.USD -> product.priceUSD
                            Currency.EUR -> product.priceEUR
                            Currency.GBP -> product.priceGBP
                        } * product.unitsSelected

                        Text(
                            text = String.format("%.2f %s", subtotal, currency.symbol),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    text = "${product.unitsSelected}",
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
fun PaymentFooter(
    seat: String,
    total: Double,
    currency: Currency,
    onSeatSelected: (String) -> Unit,
    onCashClicked: () -> Unit,
    onCardClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            SeatSelector(seat, onSeatSelected)
            TotalPrice(total, currency)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onCashClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    Text(text = "Efectivo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onCardClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CreditCard, contentDescription = null)
                    Text(text = "Tarjeta", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TotalPrice(total: Double, currency: Currency) {
    Column {
        Text(text = "TOTAL", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = String.format("%.2f %s", total, currency.symbol),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeatSelector(
    selectedSeat: String,
    onSeatSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val seats = listOf("A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3")

    Column {
        Text(text = "ASIENTO", color = Color.Gray, fontSize = 12.sp)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Button(
                onClick = { },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = selectedSeat.ifBlank { "Seleccionar" },
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