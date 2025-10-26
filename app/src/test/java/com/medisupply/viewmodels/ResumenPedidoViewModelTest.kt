package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.Cliente
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.Producto
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.adapters.ProductoConCantidad
import com.medisupply.ui.viewmodels.ResumenPedidoViewModel
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
class ResumenPedidoViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var pedidoRepository: PedidoRepository

    private lateinit var viewModel: ResumenPedidoViewModel

    private val mockCliente = Cliente(
        id = "C001",
        nombre = "Hospital General",
        nit = "901234567-8",
        logoUrl = "https://example.com/logo1.png"
    )

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
        viewModel = ResumenPedidoViewModel(pedidoRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setOrderData should update all LiveData correctly`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, mockProductos, "Entrega urgente", "V001")

        // Then
        assertEquals(mockCliente, viewModel.cliente.value)
        assertEquals(mockProductos, viewModel.productos.value)
        assertEquals("Entrega urgente", viewModel.observaciones.value)
    }

    @Test
    fun `setOrderData should calculate subtotal correctly`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // Then
        // (15.50 * 10) + (20.00 * 5) = 155.0 + 100.0 = 255.0
        assertEquals(255.0, viewModel.subtotal.value)
    }

    @Test
    fun `setOrderData should calculate impuestos correctly`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // Then
        // 255.0 * 0.10 = 25.5
        assertEquals(25.5, viewModel.impuestos.value)
    }

    @Test
    fun `setOrderData should calculate total correctly`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // Then
        // 255.0 + 25.5 = 280.5
        assertEquals(280.5, viewModel.total.value)
    }

    @Test
    fun `setOrderData should handle null observaciones`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // Then
        assertNull(viewModel.observaciones.value)
    }

    @Test
    fun `setOrderData should handle empty productos list`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, emptyList(), null, "V001")

        // Then
        assertEquals(0.0, viewModel.subtotal.value)
        assertEquals(0.0, viewModel.impuestos.value)
        assertEquals(0.0, viewModel.total.value)
    }

    @Test
    fun `setOrderData should handle single producto`() = runTest {
        // Given
        val singleProducto = listOf(mockProductos[0])

        // When
        viewModel.setOrderData(mockCliente, singleProducto, null, "V001")

        // Then
        // 15.50 * 10 = 155.0
        assertEquals(155.0, viewModel.subtotal.value)
        assertEquals(15.5, viewModel.impuestos.value)
        assertEquals(170.5, viewModel.total.value)
    }

    @Test
    fun `updateObservaciones should update observaciones LiveData`() = runTest {
        // Given
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // When
        viewModel.updateObservaciones("Nueva observación")

        // Then
        assertEquals("Nueva observación", viewModel.observaciones.value)
    }

    @Test
    fun `updateObservaciones should allow null value`() = runTest {
        // Given
        viewModel.setOrderData(mockCliente, mockProductos, "Initial", "V001")

        // When
        viewModel.updateObservaciones(null)

        // Then
        assertNull(viewModel.observaciones.value)
    }

    @Test
    fun `confirmarPedido should create pedido successfully`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockCliente, mockProductos, "Entrega urgente", "V001")

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
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockCliente, mockProductos, "Entrega urgente", "V001")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository).crearPedido(any())
    }

    @Test
    fun `confirmarPedido should set error when cliente is null`() = runTest {
        // When
        viewModel.confirmarPedido()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Datos de pedido incompletos") == true)
    }

    @Test
    fun `confirmarPedido should set error when productos is empty`() = runTest {
        // Given
        viewModel.setOrderData(mockCliente, emptyList(), null, "V001")

        // When
        viewModel.confirmarPedido()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Datos de pedido incompletos") == true)
    }

    @Test
    fun `confirmarPedido should set error on repository failure`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedido(any())).thenThrow(RuntimeException("Network error"))
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Error al crear pedido") == true)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should work with observaciones`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockCliente, mockProductos, "Observación test", "V001")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should work without observaciones`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `confirmarPedido should set isLoading correctly`() = runTest {
        // Given
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // When
        viewModel.confirmarPedido()
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        viewModel.confirmarPedido() // This will set an error

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `calculateCosts should handle large quantities`() = runTest {
        // Given
        val largeQuantityProductos = listOf(
            ProductoConCantidad(mockProductos[0].producto, 100)
        )

        // When
        viewModel.setOrderData(mockCliente, largeQuantityProductos, null, "V001")

        // Then
        // 15.50 * 100 = 1550.0
        assertEquals(1550.0, viewModel.subtotal.value)
        assertEquals(155.0, viewModel.impuestos.value)
        assertEquals(1705.0, viewModel.total.value)
    }

    @Test
    fun `calculateCosts should handle decimal prices correctly`() = runTest {
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
        viewModel.setOrderData(mockCliente, listOf(decimalPriceProducto), null, "V001")

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
    fun `productos LiveData should match setOrderData input`() = runTest {
        // When
        viewModel.setOrderData(mockCliente, mockProductos, null, "V001")

        // Then
        assertEquals(2, viewModel.productos.value?.size)
        assertEquals("P001", viewModel.productos.value?.get(0)?.producto?.id)
        assertEquals(10, viewModel.productos.value?.get(0)?.cantidad)
    }
}
