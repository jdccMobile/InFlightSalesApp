@file:OptIn(ExperimentalMaterial3Api::class)

package com.jdmobile.inflightsalesapp.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.jdmobile.inflightsalesapp.R
import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.domain.model.CustomerType
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.components.CenteredCircularProgressIndicator
import com.jdmobile.inflightsalesapp.ui.components.CenteredErrorMessage
import com.jdmobile.inflightsalesapp.ui.formatters.formatPrice
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal

@Composable
fun ProductDestination(
    onNavigateToReceipt: (cart: String, currency: String) -> Unit,
) {
    val screenActions = ProductScreenActions(
        onNavToReceipt = onNavigateToReceipt
    )


    val viewModel: ProductViewModel = koinViewModel {
        parametersOf(screenActions)
    }

    ProductScreen(
        viewModel = viewModel,
    )
}

@Composable
private fun ProductScreen(
    viewModel: ProductViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProductContent(
        state = state,
        onFilterSelected = viewModel::onFilterSelected,
        onCurrencyChanged = viewModel::onCurrencyChanged,
        onCustomerTypeChanged = viewModel::onCustomerTypeChanged,
        onProductAdded = viewModel::onProductAdded,
        onProductRemoved = viewModel::onProductRemoved,
        onPayClicked = viewModel::onPayClicked,
    )
}

@Composable
private fun ProductContent(
    state: ProductUiState,
    onFilterSelected: (ProductFilter) -> Unit,
    onCurrencyChanged: (Currency) -> Unit,
    onCustomerTypeChanged: (CustomerType) -> Unit,
    onProductAdded: (ProductId) -> Unit,
    onProductRemoved: (ProductId) -> Unit,
    onPayClicked: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.products),
                        fontWeight = FontWeight.Bold
                    )
                },
            )
        },
        bottomBar = {
            CheckoutBottomBar(
                state = state,
                onCustomerTypeChanged = onCustomerTypeChanged,
                onPayClicked = onPayClicked
            )
        }
    ) { padding ->
        when {
            state.isLoading -> CenteredCircularProgressIndicator()
            state.hasError -> CenteredErrorMessage()
            else -> {
                ProductList(
                    state = state,
                    onFilterSelected = onFilterSelected,
                    onCurrencyChanged = onCurrencyChanged,
                    onProductAdded = onProductAdded,
                    onProductRemoved = onProductRemoved,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductList(
    state: ProductUiState,
    onFilterSelected: (ProductFilter) -> Unit,
    onCurrencyChanged: (Currency) -> Unit,
    onProductAdded: (ProductId) -> Unit,
    onProductRemoved: (ProductId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            FilterSection(
                selectedFilter = state.selectedFilter,
                selectedCurrency = state.selectedCurrency,
                onFilterSelected = onFilterSelected,
                onCurrencyChanged = onCurrencyChanged
            )
        }

        items(
            items = state.filteredProducts,
            key = { it.id.value }
        ) { product ->
            ProductCard(
                product = product,
                currency = state.selectedCurrency,
                onAddProduct = { onProductAdded(product.id) },
                onRemoveProduct = { onProductRemoved(product.id) }
            )
        }
    }
}

@Composable
private fun FilterSection(
    selectedFilter: ProductFilter,
    selectedCurrency: Currency,
    onFilterSelected: (ProductFilter) -> Unit,
    onCurrencyChanged: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ProductFilterDropdown(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected
        )

        CurrencyDropdown(
            selectedCurrency = selectedCurrency,
            onCurrencyChanged = onCurrencyChanged
        )
    }
}

@Composable
private fun ProductFilterDropdown(
    selectedFilter: ProductFilter,
    onFilterSelected: (ProductFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val filters = remember {
        listOf(
            ProductFilter.ALL to R.string.all_products,
            ProductFilter.FOOD to R.string.food,
            ProductFilter.BEVERAGES to R.string.beverages
        )
    }

    DropdownSelector(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        label = stringResource(
            R.string.filter,
            stringResource(filters.first { it.first == selectedFilter }.second)
        ),
        modifier = modifier
    ) {
        filters.forEach { (filter, labelRes) ->
            DropdownMenuItem(
                text = { Text(stringResource(labelRes)) },
                onClick = {
                    onFilterSelected(filter)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun CurrencyDropdown(
    selectedCurrency: Currency,
    onCurrencyChanged: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    DropdownSelector(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        label = stringResource(R.string.currency, selectedCurrency.label),
        modifier = modifier
    ) {
        Currency.entries.forEach { currency ->
            DropdownMenuItem(
                text = { Text(currency.label) },
                onClick = {
                    onCurrencyChanged(currency)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun DropdownSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier
    ) {
        Button(
            onClick = { },
            modifier = Modifier.menuAnchor(PrimaryNotEditable),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (expanded) 180f else 0f
                    }
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            content()
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductUi,
    currency: Currency,
    onAddProduct: () -> Unit,
    onRemoveProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ProductImage(
                imageUrl = product.imageUrl,
                hasStock = product.stock > 0
            )

            ProductInfo(
                name = product.name,
                unitsSelected = product.unitsSelected,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )

            ProductActions(
                product = product,
                currency = currency,
                onAddProduct = onAddProduct,
                onRemoveProduct = onRemoveProduct,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun ProductImage(
    imageUrl: String,
    hasStock: Boolean,
    modifier: Modifier = Modifier
) {
    val colorFilter = remember(hasStock) {
        if (hasStock) null
        else ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_no_image),
        error = painterResource(R.drawable.ic_no_image),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        colorFilter = colorFilter,
        modifier = modifier.fillMaxSize()
    )
}

@Composable
private fun ProductInfo(
    name: String,
    unitsSelected: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (unitsSelected > 0) {
            Text(
                text = stringResource(R.string.units, unitsSelected),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProductActions(
    product: ProductUi,
    currency: Currency,
    onAddProduct: () -> Unit,
    onRemoveProduct: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(
            icon = Icons.Default.Remove,
            onClick = onRemoveProduct,
            backgroundColor = Color(0xFFE53935),
            isEnabled = product.unitsSelected > 0
        )

        Spacer(modifier = Modifier.width(8.dp))

        CircleIconButton(
            icon = Icons.Default.Add,
            onClick = onAddProduct,
            backgroundColor = Color(0xFF2196F3),
            isEnabled = (product.stock - product.unitsSelected) > 0
        )

        Spacer(Modifier.weight(1f))

        PriceTag(
            price = product.finalPrice,
            currency = currency
        )
    }
}

@Composable
private fun PriceTag(
    price: BigDecimal,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Gray, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = currency.formatPrice(price),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .clip(CircleShape)
            .size(size)
            .background(if (isEnabled) backgroundColor else Color.Gray)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

@Composable
private fun CheckoutBottomBar(
    state: ProductUiState,
    onCustomerTypeChanged: (CustomerType) -> Unit,
    onPayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            PayButton(
                total = state.cartSummary.total,
                currency = state.selectedCurrency,
                isEnabled = state.cart.isNotEmpty(),
                onClick = onPayClicked,
                modifier = Modifier.weight(0.7f)
            )

            CustomerTypeDropdownButton(
                customerType = state.selectedCustomerType,
                onCustomerTypeChanged = onCustomerTypeChanged,
                modifier = Modifier.weight(0.3f)
            )
        }

        CurrencyConversionInfo(state = state)
    }
}

@Composable
private fun PayButton(
    total: BigDecimal,
    currency: Currency,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            bottomStart = 24.dp,
            topEnd = 0.dp,
            bottomEnd = 0.dp
        ),
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = stringResource(
                R.string.pay,
                String.format("%.2f", total),
                currency.label
            ),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CustomerTypeDropdownButton(
    customerType: CustomerType,
    onCustomerTypeChanged: (CustomerType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .menuAnchor(PrimaryNotEditable)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A5A5A)
            ),
            shape = RoundedCornerShape(
                topStart = 0.dp,
                bottomStart = 0.dp,
                topEnd = 24.dp,
                bottomEnd = 24.dp
            ),
            contentPadding = PaddingValues(12.dp)
        ) {
            Text(
                text = stringResource(customerType.labelRes),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CustomerType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(stringResource(type.labelRes)) },
                    onClick = {
                        onCustomerTypeChanged(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CurrencyConversionInfo(
    state: ProductUiState,
    modifier: Modifier = Modifier
) {
    val conversions = remember(state.filteredProducts, state.selectedCustomerType, state.selectedCurrency) {
        calculateCurrencyConversions(state)
    }

    Text(
        text = conversions,
        modifier = modifier
    )
}

private fun calculateCurrencyConversions(state: ProductUiState): String {
    val totals = mutableMapOf(
        Currency.USD to BigDecimal.ZERO,
        Currency.EUR to BigDecimal.ZERO,
        Currency.GBP to BigDecimal.ZERO
    )

    state.filteredProducts.forEach { product ->
        val discountFactor = BigDecimal.valueOf(state.selectedCustomerType.discount)
        val quantity = BigDecimal.valueOf(product.unitsSelected.toLong())

        totals[Currency.USD] = totals[Currency.USD]!!
            .add(product.priceUSD.multiply(quantity).multiply(discountFactor))

        totals[Currency.EUR] = totals[Currency.EUR]!!
            .add(product.priceEUR.multiply(quantity).multiply(discountFactor))

        totals[Currency.GBP] = totals[Currency.GBP]!!
            .add(product.priceGBP.multiply(quantity).multiply(discountFactor))
    }

    return Currency.entries
        .filter { it != state.selectedCurrency }
        .joinToString(" | ") { currency ->
            "${String.format("%.2f", totals[currency])} ${currency.symbol}"
        }
}

private val CustomerType.labelRes: Int
    get() = when (this) {
        CustomerType.RETAIL -> R.string.customer_type_retail
        CustomerType.CREW -> R.string.customer_type_crew
        CustomerType.HAPPY_HOUR -> R.string.customer_type_happy_hour
        CustomerType.BUSINESS_INVITATION -> R.string.customer_type_business_invitation
        CustomerType.TOURIST_INVITATION -> R.string.customer_type_tourist_invitation
    }