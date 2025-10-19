package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.Producto
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.ui.viewmodels.InventarioViewModel
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
class InventarioViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var inventarioRepository: InventarioRepository

    private lateinit var viewModel: InventarioViewModel

    private val mockProductos = listOf(
        Producto(
            id = "1",
            nombre = "Alcohol en gel 500ml",
            categoria = "INSUMOS",
            imagenUrl = "https://example.com/alcohol.jpg",
            stockDisponible = 100,
            disponible = true,
            precioUnitario = "12.50",
            unidadMedida = "UNIDAD",
            descripcion = "Alcohol desinfectante"
        ),
        Producto(
            id = "2",
            nombre = "Amoxicilina 500mg",
            categoria = "MEDICAMENTOS",
            imagenUrl = "https://example.com/amoxicilina.jpg",
            stockDisponible = 50,
            disponible = true,
            precioUnitario = "35.75",
            unidadMedida = "CAJA",
            descripcion = "Antibiótico"
        ),
        Producto(
            id = "3",
            nombre = "Gasas estériles",
            categoria = "INSUMOS",
            imagenUrl = "https://example.com/gasas.jpg",
            stockDisponible = 0,
            disponible = false,
            precioUnitario = "38.00",
            unidadMedida = "CAJA",
            descripcion = "Gasas de algodón"
        ),
        Producto(
            id = "4",
            nombre = "Termómetro digital",
            categoria = "EQUIPOS",
            imagenUrl = "https://example.com/termometro.jpg",
            stockDisponible = 25,
            disponible = true,
            precioUnitario = "95.00",
            unidadMedida = "UNIDAD",
            descripcion = "Termómetro infrarrojo"
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
    fun `loadProductos should update productos and productosFiltrados with success`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)

        // When
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // Then
        assertEquals(mockProductos, viewModel.productos.value)
        assertEquals(mockProductos, viewModel.productosFiltrados.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify(inventarioRepository).getProductos()
    }

    @Test
    fun `loadProductos should extract unique categories`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)

        // When
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // Then
        val categorias = viewModel.categorias.value
        assertNotNull(categorias)
        assertEquals(3, categorias.size)
        assertTrue(categorias.contains("EQUIPOS"))
        assertTrue(categorias.contains("INSUMOS"))
        assertTrue(categorias.contains("MEDICAMENTOS"))
    }

    @Test
    fun `loadProductos should handle error`() = runTest {
        // Given
        `when`(inventarioRepository.getProductos()).thenThrow(RuntimeException("Network error"))

        // When
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar productos"))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `buscarProductos should filter by nombre`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.buscarProductos("Alcohol")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Alcohol en gel 500ml", filtrados?.first()?.nombre)
    }

    @Test
    fun `buscarProductos should be case insensitive`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.buscarProductos("alcohol")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Alcohol en gel 500ml", filtrados?.first()?.nombre)
    }

    @Test
    fun `filtrarPorCategoria should filter by specific category`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorCategoria("INSUMOS")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(2, filtrados?.size)
        assertTrue(filtrados?.all { it.categoria == "INSUMOS" } == true)
    }

    @Test
    fun `filtrarPorCategoria with null should show all products`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorCategoria("INSUMOS")
        advanceUntilIdle()
        viewModel.filtrarPorCategoria(null)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(mockProductos.size, filtrados?.size)
    }

    @Test
    fun `filtrarPorDisponibilidad with true should show only available products`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorDisponibilidad(true)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(3, filtrados?.size)
        assertTrue(filtrados?.all { it.disponible } == true)
    }

    @Test
    fun `filtrarPorDisponibilidad with false should show only unavailable products`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorDisponibilidad(false)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals(false, filtrados?.first()?.disponible)
    }

    @Test
    fun `filtrarPorDisponibilidad with null should show all products`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorDisponibilidad(true)
        advanceUntilIdle()
        viewModel.filtrarPorDisponibilidad(null)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(mockProductos.size, filtrados?.size)
    }

    @Test
    fun `should apply multiple filters simultaneously`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When - filtrar por categoría INSUMOS y disponibles
        viewModel.filtrarPorCategoria("INSUMOS")
        viewModel.filtrarPorDisponibilidad(true)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Alcohol en gel 500ml", filtrados?.first()?.nombre)
        assertEquals("INSUMOS", filtrados?.first()?.categoria)
        assertEquals(true, filtrados?.first()?.disponible)
    }

    @Test
    fun `should combine search with filters`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When - buscar "ina" + categoría MEDICAMENTOS
        viewModel.buscarProductos("ina")
        viewModel.filtrarPorCategoria("MEDICAMENTOS")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Amoxicilina 500mg", filtrados?.first()?.nombre)
    }

    @Test
    fun `limpiarFiltros should reset all filters`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When - aplicar filtros y luego limpiar
        viewModel.buscarProductos("Alcohol")
        viewModel.filtrarPorCategoria("INSUMOS")
        viewModel.filtrarPorDisponibilidad(true)
        advanceUntilIdle()
        
        viewModel.limpiarFiltros()
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(mockProductos.size, filtrados?.size)
    }

    @Test
    fun `retry should call loadProductos again`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(inventarioRepository, org.mockito.Mockito.times(2)).getProductos()
    }

    @Test
    fun `should return empty list when search has no matches`() = runTest {
        // Given
        val response = ProductoResponse(total = mockProductos.size, productos = mockProductos)
        `when`(inventarioRepository.getProductos()).thenReturn(response)
        viewModel = InventarioViewModel(inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.buscarProductos("ProductoQueNoExiste")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertTrue(filtrados?.isEmpty() == true)
    }
}


