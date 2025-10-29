package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.Producto
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.ui.adapters.ProductoConCantidad
import com.medisupply.ui.viewmodels.CrearPedidoClienteViewModel
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
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CrearPedidoClienteViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var inventarioRepository: InventarioRepository

    private lateinit var viewModel: CrearPedidoClienteViewModel

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
        ),
        Producto(
            id = "P003",
            nombre = "Aspirina 100mg",
            categoria = "MEDICAMENTOS",
            imagenUrl = "https://example.com/p003.jpg",
            stockDisponible = 75,
            disponible = true,
            precioUnitario = "10.00",
            unidadMedida = "Tableta",
            descripcion = "Anticoagulante"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = CrearPedidoClienteViewModel(inventarioRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty lists and no error`() {
        // Then
        assertNull(viewModel.searchResults.value)
        assertNotNull(viewModel.selectedProductos.value)
        assertTrue(viewModel.selectedProductos.value!!.isEmpty())
        assertNull(viewModel.error.value)
        assertNull(viewModel.isLoading.value)
    }

    @Test
    fun `searchProductos should update searchResults with valid query`() = runTest {
        // Given
        val query = "para"
        `when`(inventarioRepository.getProductos(query)).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )

        // When
        viewModel.searchProductos(query)
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertEquals(1, searchResults.size)
        assertEquals("Paracetamol 500mg", searchResults[0].nombre)
        assertEquals("P001", searchResults[0].id)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
        verify(inventarioRepository).getProductos(query)
    }

    @Test
    fun `searchProductos should return multiple results`() = runTest {
        // Given
        val query = "med"
        `when`(inventarioRepository.getProductos(query)).thenReturn(
            ProductoResponse(total = 2, productos = mockProductos.take(2))
        )

        // When
        viewModel.searchProductos(query)
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertEquals(2, searchResults.size)
    }

    @Test
    fun `searchProductos should clear results with blank query`() = runTest {
        // Given - first search with results
        `when`(inventarioRepository.getProductos("para")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )
        viewModel.searchProductos("para")
        advanceUntilIdle()

        // When - search with blank query
        viewModel.searchProductos("")
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertTrue(searchResults.isEmpty())
    }

    @Test
    fun `searchProductos should clear results with whitespace query`() = runTest {
        // When
        viewModel.searchProductos("   ")
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertTrue(searchResults.isEmpty())
    }

    @Test
    fun `searchProductos should not call repository with blank query`() = runTest {
        // When
        viewModel.searchProductos("")
        advanceUntilIdle()

        // Then
        verify(inventarioRepository, org.mockito.Mockito.never()).getProductos(any())
    }

    @Test
    fun `searchProductos should handle error and set error message`() = runTest {
        // Given
        val errorMessage = "Network error"
        `when`(inventarioRepository.getProductos(any()))
            .thenThrow(RuntimeException(errorMessage))

        // When
        viewModel.searchProductos("test")
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al buscar productos"))
        assertTrue(viewModel.error.value!!.contains(errorMessage))
        
        // Search results should be empty on error
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertTrue(searchResults.isEmpty())
        
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `searchProductos should clear previous error on success`() = runTest {
        // Given - first search fails
        `when`(inventarioRepository.getProductos("error"))
            .thenThrow(RuntimeException("Error"))
        viewModel.searchProductos("error")
        advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        // When - second search succeeds
        `when`(inventarioRepository.getProductos("para")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )
        viewModel.searchProductos("para")
        advanceUntilIdle()

        // Then
        assertNull(viewModel.error.value)
        assertEquals(1, viewModel.searchResults.value?.size)
    }

    @Test
    fun `searchProductos should handle empty results`() = runTest {
        // Given
        `when`(inventarioRepository.getProductos("nonexistent")).thenReturn(
            ProductoResponse(total = 0, productos = emptyList())
        )

        // When
        viewModel.searchProductos("nonexistent")
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertTrue(searchResults.isEmpty())
        assertNull(viewModel.error.value)
    }

    @Test
    fun `addProducto should add new producto to selectedProductos`() {
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
    fun `addProducto should not add duplicate producto`() {
        // When
        viewModel.addProducto(mockProductos[0])
        viewModel.addProducto(mockProductos[0])

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(1, selectedProductos.size)
    }

    @Test
    fun `addProducto should add multiple different productos`() {
        // When
        viewModel.addProducto(mockProductos[0])
        viewModel.addProducto(mockProductos[1])
        viewModel.addProducto(mockProductos[2])

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(3, selectedProductos.size)
        assertEquals("P001", selectedProductos[0].producto.id)
        assertEquals("P002", selectedProductos[1].producto.id)
        assertEquals("P003", selectedProductos[2].producto.id)
    }

    @Test
    fun `addProducto should initialize cantidad to 1`() {
        // When
        viewModel.addProducto(mockProductos[0])

        // Then
        val selectedProducto = viewModel.selectedProductos.value?.first()
        assertNotNull(selectedProducto)
        assertEquals(1, selectedProducto.cantidad)
    }

    @Test
    fun `updateProductoCantidad should update cantidad of existing producto`() {
        // Given
        viewModel.addProducto(mockProductos[0])

        // When
        val updatedProducto = ProductoConCantidad(mockProductos[0], 5)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(1, selectedProductos.size)
        assertEquals(5, selectedProductos[0].cantidad)
    }

    @Test
    fun `updateProductoCantidad should remove producto when cantidad is 0`() {
        // Given
        viewModel.addProducto(mockProductos[0])

        // When
        val updatedProducto = ProductoConCantidad(mockProductos[0], 0)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertTrue(selectedProductos.isEmpty())
    }

    @Test
    fun `updateProductoCantidad should remove producto when cantidad is negative`() {
        // Given
        viewModel.addProducto(mockProductos[0])

        // When
        val updatedProducto = ProductoConCantidad(mockProductos[0], -5)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertTrue(selectedProductos.isEmpty())
    }

    @Test
    fun `updateProductoCantidad should update specific producto in list`() {
        // Given
        viewModel.addProducto(mockProductos[0])
        viewModel.addProducto(mockProductos[1])
        viewModel.addProducto(mockProductos[2])

        // When - update middle product
        val updatedProducto = ProductoConCantidad(mockProductos[1], 10)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(3, selectedProductos.size)
        assertEquals(1, selectedProductos.find { it.producto.id == "P001" }?.cantidad)
        assertEquals(10, selectedProductos.find { it.producto.id == "P002" }?.cantidad)
        assertEquals(1, selectedProductos.find { it.producto.id == "P003" }?.cantidad)
    }

    @Test
    fun `updateProductoCantidad should do nothing for non-existent producto`() {
        // Given
        viewModel.addProducto(mockProductos[0])
        val initialSize = viewModel.selectedProductos.value?.size

        // When - try to update a product that's not in the list
        val nonExistentProducto = ProductoConCantidad(mockProductos[1], 5)
        viewModel.updateProductoCantidad(nonExistentProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertEquals(initialSize, selectedProductos?.size)
        assertEquals(1, selectedProductos?.find { it.producto.id == "P001" }?.cantidad)
    }

    @Test
    fun `updateProductoCantidad should maintain order when removing middle item`() {
        // Given
        viewModel.addProducto(mockProductos[0])
        viewModel.addProducto(mockProductos[1])
        viewModel.addProducto(mockProductos[2])

        // When - remove middle product
        val updatedProducto = ProductoConCantidad(mockProductos[1], 0)
        viewModel.updateProductoCantidad(updatedProducto)

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(2, selectedProductos.size)
        assertEquals("P001", selectedProductos[0].producto.id)
        assertEquals("P003", selectedProductos[1].producto.id)
    }

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given - create an error
        `when`(inventarioRepository.getProductos(any()))
            .thenThrow(RuntimeException("Error"))
        viewModel.searchProductos("test")
        advanceUntilIdle()
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
    fun `isLoading should be null initially`() {
        // Then
        assertNull(viewModel.isLoading.value)
    }

    @Test
    fun `isLoading should be false after successful search`() = runTest {
        // Given
        `when`(inventarioRepository.getProductos(any())).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )

        // When
        viewModel.searchProductos("test")
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `isLoading should be false after failed search`() = runTest {
        // Given
        `when`(inventarioRepository.getProductos(any()))
            .thenThrow(RuntimeException("Error"))

        // When
        viewModel.searchProductos("test")
        advanceUntilIdle()

        // Then
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `selectedProductos should persist across searches`() = runTest {
        // Given
        viewModel.addProducto(mockProductos[0])
        `when`(inventarioRepository.getProductos(any())).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[1]))
        )

        // When
        viewModel.searchProductos("test")
        advanceUntilIdle()

        // Then
        val selectedProductos = viewModel.selectedProductos.value
        assertNotNull(selectedProductos)
        assertEquals(1, selectedProductos.size)
        assertEquals("P001", selectedProductos[0].producto.id)
    }

    @Test
    fun `should handle rapid consecutive searches`() = runTest {
        // Given
        `when`(inventarioRepository.getProductos("a")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )
        `when`(inventarioRepository.getProductos("b")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[1]))
        )
        `when`(inventarioRepository.getProductos("c")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[2]))
        )

        // When
        viewModel.searchProductos("a")
        viewModel.searchProductos("b")
        viewModel.searchProductos("c")
        advanceUntilIdle()

        // Then - should have results from last search
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertEquals(1, searchResults.size)
        assertEquals("P003", searchResults[0].id)
    }

    @Test
    fun `should handle case sensitive searches`() = runTest {
        // Given
        `when`(inventarioRepository.getProductos("PARA")).thenReturn(
            ProductoResponse(total = 1, productos = listOf(mockProductos[0]))
        )

        // When
        viewModel.searchProductos("PARA")
        advanceUntilIdle()

        // Then
        val searchResults = viewModel.searchResults.value
        assertNotNull(searchResults)
        assertEquals(1, searchResults.size)
        verify(inventarioRepository).getProductos("PARA")
    }
}

