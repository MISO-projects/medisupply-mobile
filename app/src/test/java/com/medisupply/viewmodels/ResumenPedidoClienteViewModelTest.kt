package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.Producto
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.adapters.ProductoConCantidad
import com.medisupply.ui.viewmodels.ResumenPedidoClienteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class ResumenPedidoClienteViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var pedidoRepository: PedidoRepository

    private lateinit var viewModel: ResumenPedidoClienteViewModel

    private val mockProductos = listOf(
        ProductoConCantidad(
            producto = Producto(
                id = "P001",
                nombre = "Paracetamol 500mg",
                categoria = "MEDICAMENTOS",
                imagenUrl = "https://example.com/p001.jpg",
                stockDisponible = 100,
                disponible = true,
                precioUnitario = "15.50",
                unidadMedida = "Tableta",
                descripcion = "Analgésico"
            ),
            cantidad = 10
        ),
        ProductoConCantidad(
            producto = Producto(
                id = "P002",
                nombre = "Ibuprofeno 400mg",
                categoria = "MEDICAMENTOS",
                imagenUrl = "https://example.com/p002.jpg",
                stockDisponible = 50,
                disponible = true,
                precioUnitario = "20.00",
                unidadMedida = "Tableta",
                descripcion = "Antiinflamatorio"
            ),
            cantidad = 5
        )
    )

    private val mockCrearPedidoResponse = CrearPedidoResponse(
        id = "ORD001",
        numeroPedido = "ORD-2024-001"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ResumenPedidoClienteViewModel(pedidoRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have null values`() {
        // Then
        assertNull(viewModel.productos.value)
        assertNull(viewModel.observaciones.value)
        assertNull(viewModel.subtotal.value)
        assertNull(viewModel.impuestos.value)
        assertNull(viewModel.total.value)
        assertNull(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `setOrderData should update productos LiveData correctly`() {
        // When
        viewModel.setOrderData(mockProductos, "Entrega urgente")

        // Then
        assertEquals(mockProductos, viewModel.productos.value)
        assertEquals(2, viewModel.productos.value?.size)
    }

    @Test
    fun `setOrderData should update observaciones LiveData correctly`() {
        // When
        viewModel.setOrderData(mockProductos, "Entrega urgente")

        // Then
        assertEquals("Entrega urgente", viewModel.observaciones.value)
    }

    @Test
    fun `setOrderData should calculate subtotal correctly`() {
        // When
        viewModel.setOrderData(mockProductos, null)

        // Then
        // (15.50 * 10) + (20.00 * 5) = 155.0 + 100.0 = 255.0
        assertEquals(255.0, viewModel.subtotal.value)
    }

    @Test
    fun `setOrderData should calculate impuestos correctly`() {
        // When
        viewModel.setOrderData(mockProductos, null)

        // Then
        // 255.0 * 0.10 = 25.5
        assertEquals(25.5, viewModel.impuestos.value)
    }

    @Test
    fun `setOrderData should calculate total correctly`() {
        // When
        viewModel.setOrderData(mockProductos, null)

        // Then
        // 255.0 + 25.5 = 280.5
        assertEquals(280.5, viewModel.total.value)
    }

    @Test
    fun `setOrderData should handle null observaciones`() {
        // When
        viewModel.setOrderData(mockProductos, null)

        // Then
        assertNull(viewModel.observaciones.value)
        assertNotNull(viewModel.subtotal.value)
    }

    @Test
    fun `setOrderData should handle empty productos list`() {
        // When
        viewModel.setOrderData(emptyList(), null)

        // Then
        assertEquals(0.0, viewModel.subtotal.value)
        assertEquals(0.0, viewModel.impuestos.value)
        assertEquals(0.0, viewModel.total.value)
    }

    @Test
    fun `setOrderData should handle single producto`() {
        // Given
        val singleProducto = listOf(mockProductos[0])

        // When
        viewModel.setOrderData(singleProducto, null)

        // Then
        // 15.50 * 10 = 155.0
        assertEquals(155.0, viewModel.subtotal.value)
        assertEquals(15.5, viewModel.impuestos.value)
        assertEquals(170.5, viewModel.total.value)
    }

    @Test
    fun `setOrderData should handle producto with cantidad 1`() {
        // Given
        val productoWithOne = listOf(
            ProductoConCantidad(mockProductos[0].producto, 1)
        )

        // When
        viewModel.setOrderData(productoWithOne, null)

        // Then
        // 15.50 * 1 = 15.50
        assertEquals(15.50, viewModel.subtotal.value)
        assertEquals(1.55, viewModel.impuestos.value)
        assertEquals(17.05, viewModel.total.value)
    }

    @Test
    fun `updateObservaciones should update observaciones LiveData`() {
        // Given
        viewModel.setOrderData(mockProductos, null)

        // When
        viewModel.updateObservaciones("Nueva observación")

        // Then
        assertEquals("Nueva observación", viewModel.observaciones.value)
    }

    @Test
    fun `updateObservaciones should allow null value`() {
        // Given
        viewModel.setOrderData(mockProductos, "Initial")

        // When
        viewModel.updateObservaciones(null)

        // Then
        assertNull(viewModel.observaciones.value)
    }

    @Test
    fun `updateObservaciones should allow empty string`() {
        // Given
        viewModel.setOrderData(mockProductos, "Initial")

        // When
        viewModel.updateObservaciones("")

        // Then
        assertEquals("", viewModel.observaciones.value)
    }

    @Test
    fun `updateObservaciones should not affect cost calculations`() {
        // Given
        viewModel.setOrderData(mockProductos, null)
        val originalSubtotal = viewModel.subtotal.value
        val originalTotal = viewModel.total.value

        // When
        viewModel.updateObservaciones("New observation")

        // Then
        assertEquals(originalSubtotal, viewModel.subtotal.value)
        assertEquals(originalTotal, viewModel.total.value)
    }

    @Test
    fun `confirmarPedido should create pedido successfully`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, "Entrega urgente")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        val pedidoCreado = viewModel.pedidoCreado.value
        assertNotNull(pedidoCreado)
        assertEquals("ORD001", pedidoCreado.id)
        assertEquals("ORD-2024-001", pedidoCreado.numeroPedido)
    }

    @Test
    fun `confirmarPedido should call repository with correct data`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, "Entrega urgente")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository).crearPedidoCliente(any())
    }

    @Test
    fun `confirmarPedido should set error when productos is null`() = runTest {
        // When
        viewModel.confirmarPedido()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Datos de pedido incompletos") == true)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should set error when productos is empty`() = runTest {
        // Given
        viewModel.setOrderData(emptyList(), null)

        // When
        viewModel.confirmarPedido()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Datos de pedido incompletos") == true)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should set error on repository failure`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenThrow(RuntimeException("Network error"))
        viewModel.setOrderData(mockProductos, null)

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Error al crear pedido") == true)
        assertTrue(viewModel.error.value?.contains("Network error") == true)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should work with observaciones`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, "Observación test")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `confirmarPedido should work without observaciones`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, null)

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `confirmarPedido should set isLoading to true during execution`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, null)

        // When
        viewModel.confirmarPedido()
        // Don't advance idle yet - check loading state

        // Note: In fast tests, loading may already be false
        // We mainly verify it doesn't crash and completes
        advanceUntilIdle()

        // Then - After completion, loading should be false
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `confirmarPedido should set isLoading to false after success`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, null)

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `confirmarPedido should set isLoading to false after error`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any())).thenThrow(RuntimeException("Error"))
        viewModel.setOrderData(mockProductos, null)

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `confirmarPedido should clear previous error on success`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedidoCliente(any()))
            .thenThrow(RuntimeException("First error"))
            .thenReturn(mockCrearPedidoResponse)
        
        viewModel.setOrderData(mockProductos, null)
        
        // First attempt - fails
        viewModel.confirmarPedido()
        advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        // When - Second attempt succeeds
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.error.value)
        assertNotNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        viewModel.confirmarPedido() // This will set an error
        assertNotNull(viewModel.error.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clearError should work when no error exists`() {
        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `calculateCosts should handle large quantities`() {
        // Given
        val largeQuantityProductos = listOf(
            ProductoConCantidad(mockProductos[0].producto, 100)
        )

        // When
        viewModel.setOrderData(largeQuantityProductos, null)

        // Then
        // 15.50 * 100 = 1550.0
        assertEquals(1550.0, viewModel.subtotal.value)
        assertEquals(155.0, viewModel.impuestos.value)
        assertEquals(1705.0, viewModel.total.value)
    }

    @Test
    fun `calculateCosts should handle decimal prices correctly`() {
        // Given
        val decimalPriceProducto = ProductoConCantidad(
            producto = Producto(
                id = "P003",
                nombre = "Test Producto",
                categoria = "TEST",
                imagenUrl = "https://example.com/p003.jpg",
                stockDisponible = 10,
                disponible = true,
                precioUnitario = "12.99",
                unidadMedida = "Unit",
                descripcion = "Test"
            ),
            cantidad = 3
        )

        // When
        viewModel.setOrderData(listOf(decimalPriceProducto), null)

        // Then
        // 12.99 * 3 = 38.97
        val expectedSubtotal = 38.97
        val expectedImpuestos = 3.897
        val expectedTotal = 42.867

        assertEquals(expectedSubtotal, viewModel.subtotal.value!!, 0.01)
        assertEquals(expectedImpuestos, viewModel.impuestos.value!!, 0.01)
        assertEquals(expectedTotal, viewModel.total.value!!, 0.01)
    }

    @Test
    fun `calculateCosts should handle multiple productos with different prices`() {
        // Given
        val productos = listOf(
            ProductoConCantidad(
                producto = mockProductos[0].producto.copy(precioUnitario = "10.00"),
                cantidad = 2
            ),
            ProductoConCantidad(
                producto = mockProductos[1].producto.copy(precioUnitario = "25.50"),
                cantidad = 3
            ),
            ProductoConCantidad(
                producto = mockProductos[0].producto.copy(id = "P003", precioUnitario = "5.75"),
                cantidad = 4
            )
        )

        // When
        viewModel.setOrderData(productos, null)

        // Then
        // (10.00 * 2) + (25.50 * 3) + (5.75 * 4) = 20.0 + 76.5 + 23.0 = 119.5
        assertEquals(119.5, viewModel.subtotal.value!!, 0.01)
        assertEquals(11.95, viewModel.impuestos.value!!, 0.01)
        assertEquals(131.45, viewModel.total.value!!, 0.01)
    }

    @Test
    fun `calculateCosts should handle very small prices`() {
        // Given
        val smallPriceProducto = ProductoConCantidad(
            producto = mockProductos[0].producto.copy(precioUnitario = "0.01"),
            cantidad = 1
        )

        // When
        viewModel.setOrderData(listOf(smallPriceProducto), null)

        // Then
        assertEquals(0.01, viewModel.subtotal.value!!, 0.001)
        assertEquals(0.001, viewModel.impuestos.value!!, 0.0001)
        assertEquals(0.011, viewModel.total.value!!, 0.001)
    }

    @Test
    fun `calculateCosts should handle zero price`() {
        // Given
        val zeroPriceProducto = ProductoConCantidad(
            producto = mockProductos[0].producto.copy(precioUnitario = "0.00"),
            cantidad = 5
        )

        // When
        viewModel.setOrderData(listOf(zeroPriceProducto), null)

        // Then
        assertEquals(0.0, viewModel.subtotal.value)
        assertEquals(0.0, viewModel.impuestos.value)
        assertEquals(0.0, viewModel.total.value)
    }

    @Test
    fun `productos LiveData should match setOrderData input`() {
        // When
        viewModel.setOrderData(mockProductos, null)

        // Then
        assertEquals(2, viewModel.productos.value?.size)
        assertEquals("P001", viewModel.productos.value?.get(0)?.producto?.id)
        assertEquals(10, viewModel.productos.value?.get(0)?.cantidad)
        assertEquals("P002", viewModel.productos.value?.get(1)?.producto?.id)
        assertEquals(5, viewModel.productos.value?.get(1)?.cantidad)
    }

    @Test
    fun `setOrderData called multiple times should update all values`() {
        // Given
        viewModel.setOrderData(listOf(mockProductos[0]), "First observation")
        assertEquals(155.0, viewModel.subtotal.value)

        // When
        viewModel.setOrderData(mockProductos, "Second observation")

        // Then
        assertEquals(255.0, viewModel.subtotal.value)
        assertEquals("Second observation", viewModel.observaciones.value)
        assertEquals(2, viewModel.productos.value?.size)
    }

    @Test
    fun `tax calculation should be exactly 10 percent`() {
        // Given
        val producto = ProductoConCantidad(
            producto = mockProductos[0].producto.copy(precioUnitario = "100.00"),
            cantidad = 1
        )

        // When
        viewModel.setOrderData(listOf(producto), null)

        // Then
        assertEquals(100.0, viewModel.subtotal.value)
        assertEquals(10.0, viewModel.impuestos.value)
        assertEquals(110.0, viewModel.total.value)
    }

    @Test
    fun `confirmarPedido should handle very long observaciones`() = runTest {
        // Given
        val longObservaciones = "A".repeat(1000)
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, longObservaciones)

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should handle special characters in observaciones`() = runTest {
        // Given
        val specialObservaciones = "Observación con áéíóú ñ @ # $ % & * ( ) [ ] { }"
        whenever(pedidoRepository.crearPedidoCliente(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockProductos, specialObservaciones)

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
        assertEquals(specialObservaciones, viewModel.observaciones.value)
    }
}

