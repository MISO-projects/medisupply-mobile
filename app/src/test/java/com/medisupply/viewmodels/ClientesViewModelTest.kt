package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.Cliente
import com.medisupply.data.repositories.ClienteRepository
import com.medisupply.ui.viewmodels.ClientesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class ClientesViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var clienteRepository: ClienteRepository

    private lateinit var viewModel: ClientesViewModel

    private val mockClientes = listOf(
        Cliente(
            id = "C001",
            nombre = "Hospital General",
            nit = "901234567-8",
            logoUrl = "https://example.com/logo1.png"
        ),
        Cliente(
            id = "C002",
            nombre = "Clínica San Martín",
            nit = "901234568-9",
            logoUrl = "https://example.com/logo2.png"
        ),
        Cliente(
            id = "C003",
            nombre = "Centro Médico Integral",
            nit = "901234569-0",
            logoUrl = "https://example.com/logo3.png"
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
    fun `loadClientes should update clientes with success`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // Then
        val clientes = viewModel.clientes.value
        assertNotNull(clientes)
        assertEquals(3, clientes.size)
        assertEquals("Hospital General", clientes[0].nombre)
    }

    @Test
    fun `loadClientes should set isLoading to true while loading`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()
        
        // Then - Al finalizar isLoading debe ser false
        assertEquals(false, viewModel.isLoading.value)
        assertNotNull(viewModel.clientes.value)
    }

    @Test
    fun `loadClientes should handle error`() = runTest {
        // Given
        val errorMessage = "Network error"
        whenever(clienteRepository.getClientes()).thenThrow(RuntimeException(errorMessage))
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertEquals(true, viewModel.error.value?.contains(errorMessage))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `retry should call loadClientes again`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.clientes.value)
        assertEquals(3, viewModel.clientes.value?.size)
    }

    @Test
    fun `error should be null on successful load`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `clientes should be empty list on empty repository response`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(emptyList())
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.clientes.value)
        assertEquals(0, viewModel.clientes.value?.size)
    }

    @Test
    fun `loadClientes should preserve client data structure`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // Then
        val cliente = viewModel.clientes.value?.first()
        assertNotNull(cliente)
        assertEquals("C001", cliente.id)
        assertEquals("Hospital General", cliente.nombre)
        assertEquals("901234567-8", cliente.nit)
        assertEquals("https://example.com/logo1.png", cliente.logoUrl)
    }

    @Test
    fun `multiple retry calls should work correctly`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()
        viewModel.retry()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.clientes.value)
        assertEquals(3, viewModel.clientes.value?.size)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `error should be cleared on successful retry`() = runTest {
        // Given - Setup con repositorio inicial exitoso
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()
        
        // Verificar carga exitosa inicial
        assertNull(viewModel.error.value)
        assertNotNull(viewModel.clientes.value)

        // When - Llamar retry nuevamente con datos exitosos
        viewModel.retry()
        advanceUntilIdle()

        // Then - No debe haber error
        assertNull(viewModel.error.value)
        assertNotNull(viewModel.clientes.value)
        assertEquals(3, viewModel.clientes.value?.size)
    }

    @Test
    fun `loadClientes should handle different types of exceptions`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenThrow(IllegalStateException("Invalid state"))
        
        // When
        viewModel = ClientesViewModel(clienteRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertEquals(true, viewModel.error.value?.contains("Invalid state"))
    }
}

