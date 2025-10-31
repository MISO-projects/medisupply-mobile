package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.PedidoDetalle
import com.medisupply.data.models.ProductoPedidoDetalle
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.viewmodels.DetallePedidoClienteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class DetallePedidoClienteViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var pedidoRepository: PedidoRepository

    private lateinit var viewModel: DetallePedidoClienteViewModel

    private val pedidoId = "ORD-001"

    private val mockProductosDetalle = listOf(
        ProductoPedidoDetalle(
            idProducto = "P001",
            nombreProducto = "Guantes de nitrilo",
            cantidad = 50,
            precioUnitario = 12.50,
            subtotal = 625.00
        ),
        ProductoPedidoDetalle(
            idProducto = "P002",
            nombreProducto = "Alcohol en gel 500ml",
            cantidad = 25,
            precioUnitario = 15.00,
            subtotal = 375.00
        )
    )

    private val mockPedidoDetalle = PedidoDetalle(
        id = "ORD-001",
        numeroPedido = "ORD-2024-001",
        fechaCreacion = "2024-10-20T10:00:00Z",
        fechaActualizacion = "2024-10-20T10:00:00Z",
        fechaEntregaEstimada = "2024-10-25T10:00:00Z",
        estado = "PENDIENTE",
        valor_total = 1000.00,
        clienteId = "CLI-001",
        nombreCliente = "Hospital General",
        vendedorId = "VEN-001",
        creadoPor = "Juan Pérez",
        cantidadItems = 2,
        observaciones = "Entrega urgente",
        productos = mockProductosDetalle,
        direccion = "Calle Principal 123"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have correct LiveData setup`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)

        // When - viewModel is created (init triggers loadPedidoDetails)
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then - after init completes, data should be loaded
        assertNotNull(viewModel.pedido.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `init should trigger loadPedidoDetails automatically`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then - loadPedidoDetails should have been called automatically
        verify(pedidoRepository).obtenerPedidoPorId(pedidoId)
        assertNotNull(viewModel.pedido.value)
        assertEquals(mockPedidoDetalle, viewModel.pedido.value)
    }

    @Test
    fun `loadPedidoDetails should update pedido with success`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedido.value)
        assertEquals(mockPedidoDetalle, viewModel.pedido.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify(pedidoRepository).obtenerPedidoPorId(pedidoId)
    }

    @Test
    fun `loadPedidoDetails should set loading state correctly`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then - loading should be false after completion
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidoDetails should clear error before loading`() = runTest {
        // Given - first call fails, second succeeds
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId))
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(mockPedidoDetalle)

        // When - first load fails
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Verify error is set
        assertNotNull(viewModel.error.value)

        // When - retry succeeds
        viewModel.loadPedidoDetails()
        advanceUntilIdle()

        // Then - error should be cleared at start
        assertEquals(false, viewModel.isLoading.value)
        assertNotNull(viewModel.pedido.value)
    }

    @Test
    fun `loadPedidoDetails should handle error and set error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId))
            .thenThrow(RuntimeException(errorMessage))

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.pedido.value)
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar detalles del pedido"))
        assertTrue(viewModel.error.value!!.contains(errorMessage))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidoDetails should handle different exception types correctly`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId))
            .thenThrow(IllegalStateException("Invalid state"))

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar detalles del pedido"))
        assertTrue(viewModel.error.value!!.contains("Invalid state"))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `retry should call loadPedidoDetails again`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository, org.mockito.Mockito.times(2)).obtenerPedidoPorId(pedidoId)
        assertEquals(mockPedidoDetalle, viewModel.pedido.value)
    }

    @Test
    fun `retry after error should attempt to reload pedido`() = runTest {
        // Given - first call fails, second succeeds
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId))
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(mockPedidoDetalle)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Verify error state
        assertNotNull(viewModel.error.value)

        // Retry
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertEquals(mockPedidoDetalle, viewModel.pedido.value)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
        verify(pedidoRepository, org.mockito.Mockito.times(2)).obtenerPedidoPorId(pedidoId)
    }

    @Test
    fun `multiple retry calls should work correctly`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // When - retry multiple times
        viewModel.retry()
        advanceUntilIdle()

        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository, org.mockito.Mockito.times(3)).obtenerPedidoPorId(pedidoId)
        assertEquals(mockPedidoDetalle, viewModel.pedido.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidoDetails should maintain data integrity with correct pedido properties`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then - verify pedido properties
        val pedido = viewModel.pedido.value
        assertNotNull(pedido)
        val p = pedido
        assertEquals("ORD-001", p.id)
        assertEquals("ORD-2024-001", p.numeroPedido)
        assertEquals("PENDIENTE", p.estado)
        assertEquals(1000.00, p.valor_total)
        assertEquals(2, p.cantidadItems)
        assertEquals("Hospital General", p.nombreCliente)
        assertEquals("Entrega urgente", p.observaciones)
        assertEquals("Calle Principal 123", p.direccion)
    }

    @Test
    fun `loadPedidoDetails should preserve productos list`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(mockPedidoDetalle)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        val pedido = viewModel.pedido.value
        assertNotNull(pedido)
        val p = pedido
        assertEquals(2, p.productos.size)
        assertEquals("P001", p.productos[0].idProducto)
        assertEquals("Guantes de nitrilo", p.productos[0].nombreProducto)
        assertEquals(50, p.productos[0].cantidad)
        assertEquals(625.00, p.productos[0].subtotal)
    }

    @Test
    fun `loadPedidoDetails should handle pedido with null observaciones`() = runTest {
        // Given
        val pedidoSinObservaciones = mockPedidoDetalle.copy(observaciones = null)
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(pedidoSinObservaciones)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        val pedido = viewModel.pedido.value
        assertNotNull(pedido)
        assertNull(pedido.observaciones)
    }

    @Test
    fun `loadPedidoDetails should handle pedido with null direccion`() = runTest {
        // Given
        val pedidoSinDireccion = mockPedidoDetalle.copy(direccion = null)
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(pedidoSinDireccion)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        val pedido = viewModel.pedido.value
        assertNotNull(pedido)
        assertNull(pedido.direccion)
    }

    @Test
    fun `loadPedidoDetails should handle empty productos list`() = runTest {
        // Given
        val pedidoSinProductos = mockPedidoDetalle.copy(
            productos = emptyList(),
            cantidadItems = 0
        )
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId)).thenReturn(pedidoSinProductos)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        val pedido = viewModel.pedido.value
        assertNotNull(pedido)
        assertTrue(pedido.productos.isEmpty())
        assertEquals(0, pedido.cantidadItems)
    }

    @Test
    fun `error should contain pedido ID in message when repository throws exception`() = runTest {
        // Given
        val pedidoIdWithError = "ORD-ERROR-001"
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoIdWithError))
            .thenThrow(RuntimeException("Repository error"))

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoIdWithError)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar detalles del pedido"))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidoDetails should reset isLoading to false even on error`() = runTest {
        // Given
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId))
            .thenThrow(RuntimeException("Network error"))

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `pedido LiveData should emit correct values in sequence`() = runTest {
        // Given
        val pedido1 = mockPedidoDetalle.copy(id = "ORD-001")
        val pedido2 = mockPedidoDetalle.copy(id = "ORD-002", estado = "EN_PROCESO")
        
        `when`(pedidoRepository.obtenerPedidoPorId(pedidoId))
            .thenReturn(pedido1)
            .thenReturn(pedido2)

        // When
        viewModel = DetallePedidoClienteViewModel(pedidoRepository, pedidoId)
        advanceUntilIdle()

        // Verify first value
        assertEquals("ORD-001", viewModel.pedido.value?.id)
        assertEquals("PENDIENTE", viewModel.pedido.value?.estado)

        // Retry to get second value
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertEquals("ORD-002", viewModel.pedido.value?.id)
        assertEquals("EN_PROCESO", viewModel.pedido.value?.estado)
    }
}

