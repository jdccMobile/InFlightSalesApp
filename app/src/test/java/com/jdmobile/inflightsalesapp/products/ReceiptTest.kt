package com.jdmobile.inflightsalesapp.receipt

import app.cash.turbine.test
import com.jdmobile.inflightsalesapp.domain.model.Currency
import com.jdmobile.inflightsalesapp.domain.model.Product
import com.jdmobile.inflightsalesapp.domain.model.ProductId
import com.jdmobile.inflightsalesapp.domain.service.PaymentValidator
import com.jdmobile.inflightsalesapp.domain.usecase.GetProductsUseCase
import com.jdmobile.inflightsalesapp.domain.usecase.UpdateProductStockUseCase
import com.jdmobile.inflightsalesapp.ui.screens.product.model.CartItem
import com.jdmobile.inflightsalesapp.ui.screens.product.model.ProductFilter
import com.jdmobile.inflightsalesapp.ui.screens.receipt.ReceiptScreenActions
import com.jdmobile.inflightsalesapp.ui.screens.receipt.ReceiptViewModel
import com.jdmobile.inflightsalesapp.ui.screens.receipt.model.ReceiptInitialData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class ReceiptViewModelTest {

    @Mock
    lateinit var getProductsUseCase: GetProductsUseCase

    @Mock
    lateinit var updateProductStockUseCase: UpdateProductStockUseCase

    @Mock
    lateinit var screenActions: ReceiptScreenActions

    private lateinit var viewModel: ReceiptViewModel
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
        )
    )

    private val mockCart = listOf(
        CartItem(ProductId(1), 2, BigDecimal("5.00")),
        CartItem(ProductId(2), 1, BigDecimal("5.00"))
    )

    private val initialData = ReceiptInitialData(
        cart = mockCart,
        currency = Currency.USD
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        runBlocking {
            whenever(getProductsUseCase()).thenReturn(flowOf(mockProducts))
            whenever(updateProductStockUseCase(any(), any())).thenReturn(Unit)
        }

        viewModel = ReceiptViewModel(
            screenActions = screenActions,
            initialData = initialData,
            getProductsUseCase = getProductsUseCase,
            updateProductStockUseCase = updateProductStockUseCase,
            paymentValidator = PaymentValidator()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `products and total are loaded correctly`() = runBlocking {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.products.size)
            assertEquals(BigDecimal("10.00").setScale(2), state.total.setScale(2))
            cancel()
        }
    }

    @Test
    fun `cash payment succeeds with sufficient amount`() = runBlocking {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSeatNumberChanged("A1")
        viewModel.onCashPaymentClicked()
        viewModel.onCashAmountChanged("20.00")

        viewModel.onCashPaymentProcessed()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.showCashDialog)
            assertTrue(state.showSuccessDialog)
            cancel()
        }

        runBlocking {
            verify(updateProductStockUseCase).invoke(ProductId(1), 2)
            verify(updateProductStockUseCase).invoke(ProductId(2), 1)
        }
    }

    @Test
    fun `card payment fails with incomplete data`() = runBlocking {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onSeatNumberChanged("A1")
        viewModel.onCardPaymentClicked()
        viewModel.onCardNumberChanged("1234")

        viewModel.onCardPaymentProcessed()

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.hasValidationError)
            assertTrue(state.showCardDialog)
            cancel()
        }
    }
}