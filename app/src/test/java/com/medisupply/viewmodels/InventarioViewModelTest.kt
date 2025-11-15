package com.medisupply.viewmodels

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.R
import com.medisupply.data.models.Inventario
import com.medisupply.data.models.InventarioResponse
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.ui.viewmodels.InventarioViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class InventarioViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var inventarioRepository: InventarioRepository

    private lateinit var viewModel: InventarioViewModel
    private lateinit var application: Application

    private val mockInventario = listOf(
        Inventario(
            id = "1",
            productoId = "prod1",
            lote = "L001",
            fechaVencimiento = "2025-12-31",
            cantidad = 100,
            ubicacion = "A1",
            temperaturaRequerida = "Ambiente",
            estado = "DISPONIBLE",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Alcohol en gel 500ml",
            productoSku = "SKU001",
            categoria = "Insumos médicos",
            productoImagenUrl = "https://example.com/alcohol.jpg",
            productoUnidadMedida = "ml",
            productoTipoAlmacenamiento = "Ambiente",
            productoPrecioUnitario = "5.50",
            productoDescripcion = "Alcohol en gel para desinfección de manos"
        ),
        Inventario(
            id = "2",
            productoId = "prod2",
            lote = "L002",
            fechaVencimiento = "2025-12-31",
            cantidad = 50,
            ubicacion = "A2",
            temperaturaRequerida = "Ambiente",
            estado = "DISPONIBLE",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Amoxicilina 500mg",
            productoSku = "SKU002",
            categoria = "Medicamento",
            productoImagenUrl = "https://example.com/amoxicilina.jpg",
            productoUnidadMedida = "tableta",
            productoTipoAlmacenamiento = "Ambiente",
            productoPrecioUnitario = "12.00",
            productoDescripcion = "Antibiótico de amplio espectro"
        ),
        Inventario(
            id = "3",
            productoId = "prod3",
            lote = "L003",
            fechaVencimiento = "2025-12-31",
            cantidad = 0,
            ubicacion = "A3",
            temperaturaRequerida = "Ambiente",
            estado = "BLOQUEADO",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Gasas estériles",
            productoSku = "SKU003",
            categoria = "Insumos médicos",
            productoImagenUrl = "https://example.com/gasas.jpg",
            productoUnidadMedida = "unidad",
            productoTipoAlmacenamiento = "Ambiente",
            productoPrecioUnitario = "3.25",
            productoDescripcion = "Gasas estériles para uso médico"
        ),
        Inventario(
            id = "4",
            productoId = "prod4",
            lote = "L004",
            fechaVencimiento = "2025-12-31",
            cantidad = 25,
            ubicacion = "A4",
            temperaturaRequerida = "Ambiente",
            estado = "DISPONIBLE",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Termómetro digital",
            productoSku = "SKU004",
            categoria = "Equipamiento",
            productoImagenUrl = "https://example.com/termometro.jpg",
            productoUnidadMedida = "unidad",
            productoTipoAlmacenamiento = "Ambiente",
            productoPrecioUnitario = "25.00",
            productoDescripcion = "Termómetro digital para medición de temperatura"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        application = RuntimeEnvironment.getApplication()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProductos should update productosFiltrados with success`() = runTest {
        // Given
        val response = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        `when`(inventarioRepository.getInventario(
            page = any(),
            pageSize = any(),
            textSearch = anyOrNull(),
            estado = anyOrNull(),
            categoria = anyOrNull()
        )).thenReturn(response)

        // When
        viewModel = InventarioViewModel(application, inventarioRepository)
        viewModel.loadProductosIfNeeded()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.productosFiltrados.value)
        assertEquals(mockInventario, viewModel.productosFiltrados.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        verify(inventarioRepository).getInventario(
            page = any(),
            pageSize = any(),
            textSearch = anyOrNull(),
            estado = anyOrNull(),
            categoria = anyOrNull()
        )
    }

    @Test
    fun `loadProductos should load fixed categories`() = runTest {
        // Given
        val response = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        `when`(inventarioRepository.getInventario(any(), any(), any(), any(), any())).thenReturn(response)

        // When
        viewModel = InventarioViewModel(application, inventarioRepository)
        viewModel.loadProductosIfNeeded()
        advanceUntilIdle()

        // Then
        val categorias = viewModel.categorias.value
        assertNotNull(categorias)
        assertTrue(categorias.isNotEmpty())
        // Categories are now fixed from strings.xml
    }

    @Test
    fun `loadProductos should handle error`() = runTest {
        // Given
        `when`(inventarioRepository.getInventario(any(), any(), any(), any(), any())).thenThrow(RuntimeException("Network error"))

        // When
        viewModel = InventarioViewModel(application, inventarioRepository)
        viewModel.loadProductosIfNeeded()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cargar productos"))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `buscarProductos should filter by nombre`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 1,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, "Alcohol", null, null)).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.buscarProductos("Alcohol")
        advanceTimeBy(600) // Wait for debounce delay
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Alcohol en gel 500ml", filtrados?.first()?.productoNombre)
    }

    @Test
    fun `buscarProductos should be case insensitive`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 1,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, "alcohol", null, null)).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.buscarProductos("alcohol")
        advanceTimeBy(600) // Wait for debounce delay
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Alcohol en gel 500ml", filtrados?.first()?.productoNombre)
    }

    @Test
    fun `filtrarPorCategoria should filter by specific category`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 2,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0], mockInventario[2])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, null, null, "Insumos médicos")).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorCategoria("Insumos médicos")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(2, filtrados?.size)
        assertTrue(filtrados?.all { it.categoria == "Insumos médicos" } == true)
    }

    @Test
    fun `filtrarPorCategoria with null should show all products`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 2,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0], mockInventario[2])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, null, null, "Insumos médicos")).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorCategoria("Insumos médicos")
        advanceUntilIdle()
        viewModel.filtrarPorCategoria(null)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(mockInventario.size, filtrados?.size)
    }

    @Test
    fun `filtrarPorDisponibilidad with DISPONIBLE should show only available products`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 3,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0], mockInventario[1], mockInventario[3])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, null, "DISPONIBLE", null)).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorDisponibilidad("DISPONIBLE")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(3, filtrados?.size)
        assertTrue(filtrados?.all { it.estado == "DISPONIBLE" } == true)
    }

    @Test
    fun `filtrarPorDisponibilidad with BLOQUEADO should show only unavailable products`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 1,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[2])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, null, "BLOQUEADO", null)).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorDisponibilidad("BLOQUEADO")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("BLOQUEADO", filtrados?.first()?.estado)
    }

    @Test
    fun `filtrarPorDisponibilidad with null should show all products`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 3,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0], mockInventario[1], mockInventario[3])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, null, "DISPONIBLE", null)).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.filtrarPorDisponibilidad("DISPONIBLE")
        advanceUntilIdle()
        viewModel.filtrarPorDisponibilidad(null)
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(mockInventario.size, filtrados?.size)
    }

    @Test
    fun `should apply multiple filters simultaneously`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 1,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[0])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, null, "DISPONIBLE", "Insumos médicos")).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When - filtrar por categoría Insumos médicos y disponibles
        viewModel.filtrarPorCategoria("Insumos médicos")
        advanceUntilIdle()
        viewModel.filtrarPorDisponibilidad("DISPONIBLE")
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Alcohol en gel 500ml", filtrados?.first()?.productoNombre)
        assertEquals("Insumos médicos", filtrados?.first()?.categoria)
        assertEquals("DISPONIBLE", filtrados?.first()?.estado)
    }

    @Test
    fun `should combine search with filters`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val filteredItems = InventarioResponse(
            total = 1,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = listOf(mockInventario[1])
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, "ina", null, "Medicamento")).thenReturn(filteredItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When - buscar "ina" + categoría Medicamento
        viewModel.buscarProductos("ina")
        viewModel.filtrarPorCategoria("Medicamento")
        advanceTimeBy(600) // Wait for debounce delay
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(1, filtrados?.size)
        assertEquals("Amoxicilina 500mg", filtrados?.first()?.productoNombre)
    }

    @Test
    fun `limpiarFiltros should reset all filters`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, "Alcohol", "DISPONIBLE", "Insumos médicos")).thenReturn(
            InventarioResponse(1, 1, 20, 1, listOf(mockInventario[0]))
        )
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When - aplicar filtros y luego limpiar
        viewModel.buscarProductos("Alcohol")
        viewModel.filtrarPorCategoria("Insumos médicos")
        viewModel.filtrarPorDisponibilidad("DISPONIBLE")
        advanceTimeBy(600) // Wait for debounce delay
        advanceUntilIdle()
        
        viewModel.limpiarFiltros()
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertEquals(mockInventario.size, filtrados?.size)
    }

    @Test
    fun `retry should call loadProductos again`() = runTest {
        // Given
        val response = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        `when`(inventarioRepository.getInventario(
            page = any(),
            pageSize = any(),
            textSearch = anyOrNull(),
            estado = anyOrNull(),
            categoria = anyOrNull()
        )).thenReturn(response)
        viewModel = InventarioViewModel(application, inventarioRepository)
        viewModel.loadProductosIfNeeded()
        advanceUntilIdle()

        // When
        viewModel.retry()
        advanceUntilIdle()

        // Then
        verify(inventarioRepository, org.mockito.Mockito.times(2)).getInventario(
            page = any(),
            pageSize = any(),
            textSearch = anyOrNull(),
            estado = anyOrNull(),
            categoria = anyOrNull()
        )
    }

    @Test
    fun `should return empty list when search has no matches`() = runTest {
        // Given
        val allItems = InventarioResponse(
            total = mockInventario.size,
            page = 1,
            pageSize = 20,
            totalPages = 1,
            items = mockInventario
        )
        val emptyItems = InventarioResponse(
            total = 0,
            page = 1,
            pageSize = 20,
            totalPages = 0,
            items = emptyList()
        )
        `when`(inventarioRepository.getInventario(1, 20, null, null, null)).thenReturn(allItems)
        `when`(inventarioRepository.getInventario(1, 20, "ProductoQueNoExiste", null, null)).thenReturn(emptyItems)
        
        viewModel = InventarioViewModel(application, inventarioRepository)
        advanceUntilIdle()

        // When
        viewModel.buscarProductos("ProductoQueNoExiste")
        advanceTimeBy(600) // Wait for debounce delay
        advanceUntilIdle()

        // Then
        val filtrados = viewModel.productosFiltrados.value
        assertTrue(filtrados?.isEmpty() == true)
    }
}


