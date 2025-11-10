@file:OptIn(ExperimentalMaterial3Api::class)

package com.jdmobile.inflightsalesapp.ui.screens.product

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
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
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.ui.components.CenteredCircularProgressIndicator
import com.jdmobile.inflightsalesapp.ui.components.CenteredErrorMessage
import com.jdmobile.inflightsalesapp.ui.screens.product.model.Currency
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CustomerType
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductUi
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ProductDestination(
    onNavBack: () -> Unit,
) {
    val screenActions = ProductScreenActions(
        onNavBack = onNavBack,
    )

    val viewModel: ProductViewModel = koinViewModel(
        parameters = {
            parametersOf(screenActions)
        },
    )

    ProductScreen(viewModel)
}

@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProductContent(
        uiState = uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onCurrencySelected = viewModel::onCurrencySelected,
        onCustomerTypeSelected = viewModel::onCustomerTypeSelected,
        onAddProduct = viewModel::onAddProduct,
        onRemoveProduct = viewModel::onRemoveProduct,
        onPayClicked = viewModel::onPayClicked
    )
}

@Composable
fun ProductContent(
    uiState: ProductUiState,
    onFilterSelected: (ProductFilter) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onCustomerTypeSelected: (CustomerType) -> Unit,
    onAddProduct: (ProductId) -> Unit,
    onRemoveProduct: (ProductId) -> Unit,
    onPayClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.products),
                        fontWeight = FontWeight.Bold
                    )
                },
            )
        },
        bottomBar = {
            if (uiState.cartItemCount > 0) {
                PaySection(
                    uiState = uiState,
                    onCustomerTypeSelected = onCustomerTypeSelected,
                    onPayClicked = onPayClicked
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> CenteredCircularProgressIndicator()
            uiState.isThereError -> CenteredErrorMessage()
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                ) {
                    ProductGrid(
                        uiState,
                        onFilterSelected,
                        onCurrencySelected,
                        onAddProduct,
                        onRemoveProduct
                    )
                }
            }
        }
    }
}

@Composable
private fun Filters(
    uiState: ProductUiState,
    onFilterSelected: (ProductFilter) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProductFilterDropdown(
            selectedFilter = uiState.selectedFilter,
            onFilterSelected = onFilterSelected,
        )

        CurrencySelectorDropdown(
            selectedCurrency = uiState.selectedCurrency,
            onCurrencySelected = onCurrencySelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFilterDropdown(
    selectedFilter: ProductFilter,
    onFilterSelected: (ProductFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val options = listOf(
        ProductFilter.ALL to stringResource(R.string.all_products),
        ProductFilter.FOOD to stringResource(R.string.food),
        ProductFilter.BEVERAGES to stringResource(R.string.beverages)
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        R.string.filter,
                        options.first { it.first == selectedFilter }.second
                    ),
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.dropdown_arrow),
                    tint = Color.Black,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (expanded) 180f else 0f
                    }
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (filter, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelectorDropdown(
    selectedCurrency: Currency,
    onCurrencySelected: (Currency) -> Unit,
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0E0E0)
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.currency, selectedCurrency.label),
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(R.string.dropdown_arrow),
                    tint = Color.Black,
                    modifier = Modifier.graphicsLayer {
                        rotationZ = if (expanded) 180f else 0f
                    }
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.label) },
                    onClick = {
                        onCurrencySelected(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    uiState: ProductUiState,
    onFilterSelected: (ProductFilter) -> Unit,
    onCurrencySelected: (Currency) -> Unit,
    onAddProduct: (ProductId) -> Unit,
    onRemoveProduct: (ProductId) -> Unit,
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
            Filters(
                uiState = uiState,
                onFilterSelected = onFilterSelected,
                onCurrencySelected = onCurrencySelected
            )
        }

        items(uiState.products) { product ->
            ProductCard(
                productUi = product,
                selectedCurrency = uiState.selectedCurrency,
                onAddProduct = { onAddProduct(product.id) },
                onRemoveProduct = { onRemoveProduct(product.id) }
            )
        }
    }
}

@Composable
private fun ProductCard(
    productUi: ProductUi,
    selectedCurrency: Currency,
    onAddProduct: () -> Unit,
    onRemoveProduct: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            ProductCardImage(productUi.imageUrl)

            ProductCardInfo(
                productUi = productUi,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
            )

            ProductCardButtons(
                onRemoveProduct = onRemoveProduct,
                onAddProduct = onAddProduct,
                productUi = productUi,
                selectedCurrency = selectedCurrency,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            )
        }
    }
}

@Composable
private fun ProductCardImage(imageUrl: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        placeholder = painterResource(R.drawable.ic_no_image),
        error = painterResource(R.drawable.ic_no_image),
        contentDescription = stringResource(R.string.product_image),
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun ProductCardInfo(
    productUi: ProductUi,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = productUi.name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Text(
            text = stringResource(R.string.units, productUi.unit),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProductCardButtons(
    onRemoveProduct: () -> Unit,
    onAddProduct: () -> Unit,
    productUi: ProductUi,
    selectedCurrency: Currency,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleIconButton(
            icon = Icons.Default.Remove,
            contentDescription = stringResource(R.string.remove),
            backgroundColor = Color(0xFFE53935),
            onClick = onRemoveProduct
        )

        Spacer(modifier = Modifier.width(8.dp))

        CircleIconButton(
            icon = Icons.Default.Add,
            contentDescription = stringResource(R.string.add),
            backgroundColor = Color(0xFF2196F3),
            onClick = onAddProduct
        )

        Spacer(Modifier.weight(1f))

        PriceTag(
            productUi = productUi,
            selectedCurrency = selectedCurrency
        )
    }
}

@Composable
private fun PriceTag(
    productUi: ProductUi,
    selectedCurrency: Currency
) {
    Box(
        modifier = Modifier.background(Color.Gray, RoundedCornerShape(16.dp))
    ) {
        Text(
            text = productUi.getFormattedPrice(selectedCurrency),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
    size: Dp = 32.dp,
    iconPadding: Dp = 8.dp
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .size(size)
            .background(backgroundColor)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.padding(iconPadding)
        )
    }
}

@Composable
private fun PaySection(
    uiState: ProductUiState,
    onCustomerTypeSelected: (CustomerType) -> Unit,
    onPayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BottomCheckoutBar(
            total = uiState.cartTotal,
            currency = uiState.selectedCurrency,
            customerType = uiState.selectedCustomerType,
            onCustomerTypeSelected = onCustomerTypeSelected,
            onPayClicked = onPayClicked,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = buildConversionText(uiState),
        )
    }
}

private fun buildConversionText(uiState: ProductUiState): String {
    val conversions = mutableListOf<String>()

    val totals = mutableMapOf(
        Currency.USD to 0.0,
        Currency.EUR to 0.0,
        Currency.GBP to 0.0
    )

    uiState.products.forEach { product ->
        val discount = uiState.selectedCustomerType.discount
        totals[Currency.USD] =
            totals[Currency.USD]!! + (product.priceUSD * product.quantity * discount)
        totals[Currency.EUR] =
            totals[Currency.EUR]!! + (product.priceEUR * product.quantity * discount)
        totals[Currency.GBP] =
            totals[Currency.GBP]!! + (product.priceGBP * product.quantity * discount)
    }

    val otherCurrencies = Currency.entries.filter { it != uiState.selectedCurrency }

    otherCurrencies.forEach { currency ->
        val total = totals[currency] ?: 0.0
        conversions.add(String.format("%.2f %s", total, currency.symbol))
    }

    return conversions.joinToString(" | ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomCheckoutBar(
    total: Double,
    currency: Currency,
    customerType: CustomerType,
    onCustomerTypeSelected: (CustomerType) -> Unit,
    onPayClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PayButton(
            onPayClicked = onPayClicked,
            total = total,
            currency = currency,
            modifier = Modifier.weight(0.7f)
        )

        DiscountButton(
            customerType = customerType,
            onCustomerTypeSelected = onCustomerTypeSelected,
            modifier = Modifier.weight(0.3f)
        )
    }
}

@Composable
private fun PayButton(
    onPayClicked: () -> Unit,
    total: Double,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onPayClicked,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(
            topStart = 24.dp, bottomStart = 24.dp,
            topEnd = 0.dp, bottomEnd = 0.dp
        ),
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.pay, String.format("%.2f", total), currency.label),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DiscountButton(
    customerType: CustomerType,
    onCustomerTypeSelected: (CustomerType) -> Unit,
    modifier: Modifier = Modifier,
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
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5A5A5A)
            ),
            shape = RoundedCornerShape(
                topStart = 0.dp, bottomStart = 0.dp,
                topEnd = 24.dp, bottomEnd = 24.dp
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(customerType.getLabelRes()),
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
                    text = {
                        Text(
                            text = stringResource(type.getLabelRes()),
                        )
                    },
                    onClick = {
                        onCustomerTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun CustomerType.getLabelRes(): Int = when (this) {
    CustomerType.RETAIL -> R.string.customer_type_retail
    CustomerType.CREW -> R.string.customer_type_crew
    CustomerType.HAPPY_HOUR -> R.string.customer_type_happy_hour
    CustomerType.BUSINESS_INVITATION -> R.string.customer_type_business_invitation
    CustomerType.TOURIST_INVITATION -> R.string.customer_type_tourist_invitation
}