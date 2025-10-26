package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.Pedido
import com.medisupply.data.models.PedidoItem
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.viewmodels.PedidosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class PedidosViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var pedidoRepository: PedidoRepository

    private lateinit var viewModel: PedidosViewModel

    private val mockPedidoItems = listOf(
        PedidoItem(
            idProducto = "P001",
            cantidad = 10,
            precioUnitario = 15.50
        ),
        PedidoItem(
            idProducto = "P002",
            cantidad = 5,
            precioUnitario = 25.00
        )
    )

    private val mockPedidos = listOf(
        Pedido(
            id = "ORD001",
            numeroPedido = "ORD-2024-001",
            fechaCreacion = "2024-10-20T10:00:00Z",
            fechaActualizacion = "2024-10-20T10:00:00Z",
            fechaEntregaEstimada = "2024-10-25T10:00:00Z",
            estado = "PENDIENTE",
            valor_total = 280.00,
            clienteId = "C001",
            nombreCliente = "Hospital General",
            vendedorId = "V001",
            creadoPor = "Juan Pérez",
            cantidadItems = 2,
            observaciones = "Entrega urgente",
            productos = mockPedidoItems
        ),
        Pedido(
            id = "ORD002",
            numeroPedido = "ORD-2024-002",
            fechaCreacion = "2024-10-21T14:30:00Z",
            fechaActualizacion = "2024-10-21T14:30:00Z",
            fechaEntregaEstimada = "2024-10-26T14:30:00Z",
            estado = "EN_PROCESO",
            valor_total = 450.00,
            clienteId = "C002",
            nombreCliente = "Clínica San Martín",
            vendedorId = "V001",
            creadoPor = "María López",
            cantidadItems = 3,
            observaciones = null,
            productos = mockPedidoItems
        ),
        Pedido(
            id = "ORD003",
            numeroPedido = "ORD-2024-003",
            fechaCreacion = "2024-10-22T09:15:00Z",
            fechaActualizacion = "2024-10-22T09:15:00Z",
            fechaEntregaEstimada = "2024-10-27T09:15:00Z",
            estado = "COMPLETADO",
            valor_total = 320.50,
            clienteId = "C003",
            nombreCliente = "Centro Médico Integral",
            vendedorId = "V002",
            creadoPor = "Carlos Gómez",
            cantidadItems = 4,
            observaciones = "Cliente frecuente",
            productos = mockPedidoItems
        )
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
    fun `init should load pedidos automatically`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val pedidos = viewModel.pedidos.value
        assertNotNull(pedidos)
        assertEquals(3, pedidos.size)
        verify(pedidoRepository).listarPedidos()
    }

    @Test
    fun `loadPedidos should update pedidos LiveData`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val pedidos = viewModel.pedidos.value
        assertEquals(3, pedidos?.size)
        assertEquals("ORD-2024-001", pedidos?.get(0)?.numeroPedido)
    }

    @Test
    fun `loadPedidos should return pedidos with correct structure`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val primerPedido = viewModel.pedidos.value?.first()
        assertNotNull(primerPedido)
        assertNotNull(primerPedido.id)
        assertNotNull(primerPedido.numeroPedido)
        assertNotNull(primerPedido.estado)
        assertNotNull(primerPedido.nombreCliente)
    }

    @Test
    fun `loadPedidos should return pedidos with different estados`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val estados = viewModel.pedidos.value?.map { it.estado }
        assertTrue(estados?.contains("PENDIENTE") == true)
        assertTrue(estados?.contains("EN_PROCESO") == true)
        assertTrue(estados?.contains("COMPLETADO") == true)
    }

    @Test
    fun `loadPedidos should handle empty list`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(emptyList())

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val pedidos = viewModel.pedidos.value
        assertTrue(pedidos.isNullOrEmpty())
    }

    @Test
    fun `loadPedidos should set error on failure`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenThrow(RuntimeException("Network error"))

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Error al cargar pedidos") == true)
    }

    @Test
    fun `loadPedidos should clear error on success`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos())
            .thenThrow(RuntimeException("Error"))
            .thenReturn(mockPedidos)
        
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.loadPedidos()
        advanceUntilIdle()

        // Then
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun `loadPedidos should set isLoading correctly`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `retry should call loadPedidos again`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository, org.mockito.kotlin.times(2)).listarPedidos()
    }

    @Test
    fun `retry should recover from previous error`() = runTest {
        // Given - first call fails, second succeeds
        whenever(pedidoRepository.listarPedidos())
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(mockPedidos)
        
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()
        
        // Verify error state
        assertNotNull(viewModel.error.value)

        // When - retry with success
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidos.value)
        assertEquals(3, viewModel.pedidos.value?.size)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun `pedidos should contain cliente information`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        viewModel.pedidos.value?.forEach { pedido ->
            assertNotNull(pedido.clienteId)
            assertNotNull(pedido.nombreCliente)
        }
    }

    @Test
    fun `pedidos should contain vendedor information`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        viewModel.pedidos.value?.forEach { pedido ->
            assertNotNull(pedido.vendedorId)
            assertNotNull(pedido.creadoPor)
        }
    }

    @Test
    fun `pedidos should contain productos list`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        viewModel.pedidos.value?.forEach { pedido ->
            assertNotNull(pedido.productos)
            assertTrue(pedido.productos.isNotEmpty())
        }
    }

    @Test
    fun `pedidos should have valid numero pedido format`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        viewModel.pedidos.value?.forEach { pedido ->
            assertTrue(pedido.numeroPedido.startsWith("ORD-"))
            assertTrue(pedido.numeroPedido.length > 10)
        }
    }

    @Test
    fun `pedidos should have unique IDs`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val ids = viewModel.pedidos.value?.map { it.id }
        assertEquals(ids?.size, ids?.distinct()?.size, "All pedido IDs should be unique")
    }

    @Test
    fun `pedidos should have valid timestamps`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        viewModel.pedidos.value?.forEach { pedido ->
            assertTrue(pedido.fechaCreacion.isNotEmpty())
            assertTrue(pedido.fechaActualizacion.isNotEmpty())
            assertTrue(pedido.fechaEntregaEstimada.isNotEmpty())
        }
    }

    @Test
    fun `pedidos should have positive valor_total`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        viewModel.pedidos.value?.forEach { pedido ->
            assertTrue(pedido.valor_total > 0.0)
        }
    }

    @Test
    fun `loadPedidos should handle observaciones correctly`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val primerPedido = viewModel.pedidos.value?.get(0)
        assertNotNull(primerPedido?.observaciones)
        assertEquals("Entrega urgente", primerPedido?.observaciones)
        
        val segundoPedido = viewModel.pedidos.value?.get(1)
        assertEquals(null, segundoPedido?.observaciones)
    }

    @Test
    fun `loadPedidos should maintain order from repository`() = runTest {
        // Given
        whenever(pedidoRepository.listarPedidos()).thenReturn(mockPedidos)

        // When
        viewModel = PedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        val pedidos = viewModel.pedidos.value
        assertEquals("ORD001", pedidos?.get(0)?.id)
        assertEquals("ORD002", pedidos?.get(1)?.id)
        assertEquals("ORD003", pedidos?.get(2)?.id)
    }
}
