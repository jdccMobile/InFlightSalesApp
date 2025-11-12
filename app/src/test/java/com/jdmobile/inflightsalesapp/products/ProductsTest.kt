package com.jdmobile.inflightsalesapp.products

import app.cash.turbine.test
import com.jdmobile.inflightsalesapp.domain.model.Product
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.service.PriceCalculator
import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.SyncProductsUseCase
import com.jdmobile.inflightsalesapp.ui.screens.product.ProductScreenActions
import com.jdmobile.inflightsalesapp.ui.screens.product.ProductSyncState
import com.jdmobile.inflightsalesapp.ui.screens.product.ProductViewModel
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import arrow.core.right
import java.math.BigDecimal

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ProductViewModelTest {

    @Mock
    lateinit var getProductsUseCase: GetProductsUseCase

    @Mock
    lateinit var syncProductsUseCase: SyncProductsUseCase

    @Mock
    lateinit var screenActions: ProductScreenActions

    private lateinit var viewModel: ProductViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockProducts = listOf(
        Product(
            id = ProductId(1),
            name = "Coca Cola",
            stock = 10,
            priceUSD = BigDecimal("2.50"),
            priceEUR = BigDecimal("2.30"),
            priceGBP = BigDecimal("2.00"),
            imageUrl = "https://example.com/coke.jpg",
            category = ProductFilter.BEVERAGES
        ),
        Product(
            id = ProductId(2),
            name = "Sandwich",
            stock = 5,
            priceUSD = BigDecimal("5.00"),
            priceEUR = BigDecimal("4.50"),
            priceGBP = BigDecimal("4.00"),
            imageUrl = "https://example.com/sandwich.jpg",
            category = ProductFilter.FOOD
        ),
        Product(
            id = ProductId(3),
            name = "Water",
            stock = 20,
            priceUSD = BigDecimal("1.50"),
            priceEUR = BigDecimal("1.30"),
            priceGBP = BigDecimal("1.10"),
            imageUrl = "https://example.com/water.jpg",
            category = ProductFilter.BEVERAGES
        )
    )

    @Before
    fun setUp() = runTest {
        Dispatchers.setMain(testDispatcher)
        ProductSyncState.isSynced = false

        whenever(getProductsUseCase()).thenReturn(flowOf(mockProducts))
        whenever(syncProductsUseCase()).thenReturn(Unit.right())

        viewModel = ProductViewModel(
            screenActions = screenActions,
            getProductsUseCase = getProductsUseCase,
            syncProductsUseCase = syncProductsUseCase,
            priceCalculator = PriceCalculator()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        ProductSyncState.isSynced = false
    }

    @Test
    fun `Loading state is updated after products are loaded`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals(3, loadedState.allProducts.size)
            cancel()
        }
    }

    @Test
    fun `Filter by BEVERAGES shows only beverages`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onFilterSelected(ProductFilter.BEVERAGES)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(ProductFilter.BEVERAGES, state.selectedFilter)
            assertEquals(2, state.filteredProducts.size)
            assertTrue(state.filteredProducts.all { it.category == ProductFilter.BEVERAGES })
            cancel()
        }
    }

    @Test
    fun `Adding product to cart updates cart and summary correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val productId = ProductId(1)
        viewModel.onProductAdded(productId)

        viewModel.state.test {
            val state = awaitItem()

            assertEquals(1, state.cart.size)
            assertEquals(productId, state.cart.first().productId)
            assertEquals(1, state.cart.first().quantity)
            assertEquals(1, state.cartSummary.itemCount)
            assertEquals(BigDecimal("2.50").setScale(2), state.cartSummary.total.setScale(2))
            cancel()
        }
    }
}