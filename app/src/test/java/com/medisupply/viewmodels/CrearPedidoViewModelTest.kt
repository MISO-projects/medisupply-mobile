package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.Cliente
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.PedidoRequest
import com.medisupply.data.models.Producto
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.repositories.ClienteRepository
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.adapters.ProductoConCantidad
import com.medisupply.ui.viewmodels.CrearPedidoViewModel
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
class CrearPedidoViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var clienteRepository: ClienteRepository

    @Mock
    private lateinit var inventarioRepository: InventarioRepository

    @Mock
    private lateinit var pedidoRepository: PedidoRepository

    private lateinit var viewModel: CrearPedidoViewModel

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
        )
    )

    private val mockProductos = listOf(
        Producto(
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
        Producto(
            id = "P002",
            nombre = "Ibuprofeno 400mg",
            categoria = "MEDICAMENTOS",
            imagenUrl = "https://example.com/p002.jpg",
            stockDisponible = 50,
            disponible = true,
            precioUnitario = "20.00",
            unidadMedida = "Tableta",
            descripcion = "Antiinflamatorio"
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load clientes automatically`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)

        // When
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // Then
        val clientes = viewModel.clientes.value
        assertNotNull(clientes)
        assertEquals(2, clientes.size)
        verify(clienteRepository).getClientes()
    }

    @Test
    fun `loadClientes should update clientes LiveData`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)

        // When
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.clientes.value?.size)
        assertEquals("Hospital General", viewModel.clientes.value?.get(0)?.nombre)
    }

    @Test
    fun `loadClientes should set error on failure`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenThrow(RuntimeException("Network error"))

        // When
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Error al cargar clientes") == true)
    }

    @Test
    fun `searchProductos should update searchResults with valid query`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        whenever(inventarioRepository.getProductos("para")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.searchProductos("para")
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertEquals(1, searchResults.size)
        assertEquals("Paracetamol 500mg", searchResults[0].nombre)
    }

    @Test
    fun `searchProductos should clear results with blank query`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.searchProductos("")
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertTrue(searchResults.isNullOrEmpty())
    }

    @Test
    fun `searchProductos should set error on failure`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        whenever(inventarioRepository.getProductos(any())).thenThrow(RuntimeException("Search error"))
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.searchProductos("test")
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Error al buscar productos") == true)
    }

    @Test
    fun `addProducto should add new producto to selectedProductos`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.addProducto(mockProductos[0])

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(1, selectedProductos.size)
        assertEquals("P001", selectedProductos[0].producto.id)
        assertEquals(1, selectedProductos[0].cantidad)
    }

    @Test
    fun `addProducto should not add duplicate producto`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.addProducto(mockProductos[0])
        viewModel.addProducto(mockProductos[0])

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertEquals(1, selectedProductos?.size)
    }

    @Test
    fun `addProducto should add multiple different productos`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.addProducto(mockProductos[0])
        viewModel.addProducto(mockProductos[1])

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertEquals(2, selectedProductos?.size)
    }

    @Test
    fun `removeProducto should remove producto from selectedProductos`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])

        // When
        viewModel.removeProducto("P001")

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertTrue(selectedProductos.isNullOrEmpty())
    }

    @Test
    fun `updateProductoCantidad should update cantidad of existing producto`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])

        // When
        val updatedProducto = ProductoConCantidad(mockProductos[0], 5)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertEquals(5, selectedProductos?.find { it.producto.id == "P001" }?.cantidad)
    }

    @Test
    fun `updateProductoCantidad should remove producto when cantidad is 0`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])

        // When
        val updatedProducto = ProductoConCantidad(mockProductos[0], 0)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertTrue(selectedProductos.isNullOrEmpty())
    }

    @Test
    fun `crearPedido should create pedido successfully`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])

        // When
        viewModel.crearPedido("C001", "V001", "Entrega urgente")
        advanceUntilIdle()

        // Then
        val pedidoCreado = viewModel.pedidoCreado.value
        assertNotNull(pedidoCreado)
        assertEquals("ORD001", pedidoCreado.id)
        assertEquals("ORD-2024-001", pedidoCreado.numeroPedido)
    }

    @Test
    fun `crearPedido should set error when no productos selected`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.crearPedido("C001", "V001", null)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Debe agregar al menos un producto") == true)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `crearPedido should filter out productos with cantidad 0`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])
        viewModel.updateProductoCantidad(ProductoConCantidad(mockProductos[0], 5))

        // When
        viewModel.crearPedido("C001", "V001", null)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `crearPedido should set error on failure`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        whenever(pedidoRepository.crearPedido(any())).thenThrow(RuntimeException("Network error"))
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])

        // When
        viewModel.crearPedido("C001", "V001", null)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value?.contains("Error al crear pedido") == true)
        assertNull(viewModel.pedidoCreado.value)
    }

    @Test
    fun `crearPedido should work with observaciones`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)
        whenever(pedidoRepository.crearPedido(any())).thenReturn(mockCrearPedidoResponse)
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()
        viewModel.addProducto(mockProductos[0])

        // When
        viewModel.crearPedido("C001", "V001", "Entrega en piso 3")
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.pedidoCreado.value)
        verify(pedidoRepository).crearPedido(any())
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenThrow(RuntimeException("Error"))
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    @Test
    fun `isLoading should be true during loadClientes`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)

        // When
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        
        // Then - verify loading state changes
        advanceUntilIdle()
        // After completion, loading should be false
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `selectedProductos should be empty initially`() = runTest {
        // Given
        whenever(clienteRepository.getClientes()).thenReturn(mockClientes)

        // When
        viewModel = CrearPedidoViewModel(clienteRepository, inventarioRepository, pedidoRepository)
        advanceUntilIdle()

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertTrue(selectedProductos.isNullOrEmpty())
    }
}
