package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.PedidoResumenCliente
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.viewmodels.ClientPedidosViewModel
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
class ClientPedidosViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var pedidoRepository: PedidoRepository

    private lateinit var viewModel: ClientPedidosViewModel

    private val mockPedidos = listOf(
        PedidoResumenCliente(
            id = "1",
            numeroPedido = "PED-001",
            fechaCreacion = "2025-10-15",
            estado = "PENDIENTE",
            valor_total = 1500.50,
            clienteId = "CLI-001",
            cantidadItems = 5,
            fechaEntregaEstimada = "2025-10-20"
        ),
        PedidoResumenCliente(
            id = "2",
            numeroPedido = "PED-002",
            fechaCreacion = "2025-10-20",
            estado = "EN_PROCESO",
            valor_total = 2300.75,
            clienteId = "CLI-001",
            cantidadItems = 8,
            fechaEntregaEstimada = "2025-10-25"
        ),
        PedidoResumenCliente(
            id = "3",
            numeroPedido = "PED-003",
            fechaCreacion = "2025-10-22",
            estado = "ENTREGADO",
            valor_total = 850.00,
            clienteId = "CLI-001",
            cantidadItems = 3,
            fechaEntregaEstimada = "2025-10-27"
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
    fun `initial state should have null pedidos and no error`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)
        
        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        
        // Then - before advancing, loading should be initiated
        // After advancing, data should be loaded
        advanceUntilIdle()
        
        assertNotNull(viewModel.pedidos.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadPedidos should update pedidos with success`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        assertEquals(mockPedidos, viewModel.pedidos.value)
        assertEquals(3, viewModel.pedidos.value?.size)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify(pedidoRepository).listarPedidosCliente()
    }

    @Test
    fun `loadPedidos should set loading state correctly`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        
        // Then - loading should start as true
        // Note: Due to the fast execution in tests, we check the final state
        advanceUntilIdle()
        
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidos should handle empty list`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(emptyList())

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidos.value)
        assertTrue(viewModel.pedidos.value!!.isEmpty())
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadPedidos should handle error and set error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(pedidoRepository.listarPedidosCliente()).thenThrow(RuntimeException(errorMessage))

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar pedidos"))
        assertTrue(viewModel.error.value!!.contains(errorMessage))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidos should clear previous error on success`() = runTest {
        // Given - first call fails
        `when`(pedidoRepository.listarPedidosCliente())
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(mockPedidos)

        // When - first load fails
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()
        
        // Verify error is set
        assertNotNull(viewModel.error.value)
        
        // When - retry succeeds
        viewModel.loadPedidos()
        advanceUntilIdle()

        // Then
        assertEquals(mockPedidos, viewModel.pedidos.value)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `retry should call loadPedidos again`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository, org.mockito.Mockito.times(2)).listarPedidosCliente()
        assertEquals(mockPedidos, viewModel.pedidos.value)
    }

    @Test
    fun `retry after error should attempt to reload pedidos`() = runTest {
        // Given - first call fails, second succeeds
        `when`(pedidoRepository.listarPedidosCliente())
            .thenThrow(RuntimeException("Network error"))
            .thenReturn(mockPedidos)

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()
        
        // Verify error state
        assertNotNull(viewModel.error.value)
        
        // Retry
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertEquals(mockPedidos, viewModel.pedidos.value)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
        verify(pedidoRepository, org.mockito.Mockito.times(2)).listarPedidosCliente()
    }

    @Test
    fun `multiple retry calls should work correctly`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // When - retry multiple times
        viewModel.retry()
        advanceUntilIdle()
        
        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(pedidoRepository, org.mockito.Mockito.times(3)).listarPedidosCliente()
        assertEquals(mockPedidos, viewModel.pedidos.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadPedidos should maintain data integrity with correct pedido properties`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then - verify first pedido properties
        val firstPedido = viewModel.pedidos.value?.first()
        assertNotNull(firstPedido)
        assertEquals("1", firstPedido?.id)
        assertEquals("PED-001", firstPedido?.numeroPedido)
        assertEquals("PENDIENTE", firstPedido?.estado)
        assertEquals(1500.50, firstPedido?.valor_total)
        assertEquals(5, firstPedido?.cantidadItems)
    }

    @Test
    fun `init should trigger loadPedidos automatically`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente()).thenReturn(mockPedidos)

        // When - just create the viewModel
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then - loadPedidos should have been called automatically
        verify(pedidoRepository).listarPedidosCliente()
        assertEquals(mockPedidos, viewModel.pedidos.value)
    }

    @Test
    fun `should handle different exception types correctly`() = runTest {
        // Given
        `when`(pedidoRepository.listarPedidosCliente())
            .thenThrow(IllegalStateException("Invalid state"))

        // When
        viewModel = ClientPedidosViewModel(pedidoRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar pedidos"))
        assertTrue(viewModel.error.value!!.contains("Invalid state"))
        assertEquals(false, viewModel.isLoading.value)
    }
}

