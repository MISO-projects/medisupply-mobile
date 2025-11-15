package com.medisupply.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@ExperimentalCoroutinesApi
class VisitasViewModelTest {

    // Regla para que LiveData funcione en tests unitarios
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    // Mocks
    private lateinit var repository: VisitasRepository
    private lateinit var sessionManager: SessionManager

    // SUT (Subject Under Test)
    private lateinit var viewModel: VisitasViewModel

    // Formateador y datos de prueba
    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val testDate: Date = Calendar.getInstance().time
    private val testDateFormatted: String = apiDateFormatter.format(testDate)
    private val testVendedorId = "vendedor-123"

    private val defaultLat = 7.138458
    private val defaultLon = -73.124227

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        sessionManager = mock()

        // Configuración por defecto de los mocks
        runTest {
            whenever(sessionManager.getIdSeller()).thenReturn(testVendedorId)
        }

        viewModel = VisitasViewModel(repository, sessionManager)
        // Seteamos la fecha inicial
        viewModel.seleccionarFecha(testDate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() 
    }

    @Test
    fun `retry - EXITO - debe cargar rutas con coordenadas por defecto`() = runTest {
        // Arrange
        val mockRutas = listOf(
            RutaVisitaItem("1", "c1", "Hospital", "Dir 1", "15 min", "PENDIENTE")
        )
        whenever(repository.getRutasDelDia(eq(testDateFormatted), eq(testVendedorId), eq(defaultLat), eq(defaultLon)))
            .thenReturn(mockRutas)

        // Act
        viewModel.retry() // retry() llama a cargarRutasParaFechaSeleccionada con lat/lon fijas

        // Assert
        assertEquals(false, viewModel.isLoading.value) // Debe estar falso al terminar
        assertNull(viewModel.error.value) // No debe haber error
        assertEquals(mockRutas, viewModel.rutas.value) // Las rutas deben cargarse

        verify(repository).getRutasDelDia(eq(testDateFormatted), eq(testVendedorId), eq(defaultLat), eq(defaultLon))
    }

    @Test
    fun `cargarRutasParaFechaSeleccionada - EXITO - debe cargar rutas sin optimizar (null lat-lon)`() = runTest {
        // Arrange
        val mockRutas = listOf(
            RutaVisitaItem("1", "c1", "Hospital", "Dir 1", "Sin calcular", "PENDIENTE")
        )
        whenever(repository.getRutasDelDia(eq(testDateFormatted), eq(testVendedorId), eq(null), eq(null)))
            .thenReturn(mockRutas)

        viewModel.cargarRutasParaFechaSeleccionada(null, null)

        // Assert
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(mockRutas, viewModel.rutas.value)
        verify(repository).getRutasDelDia(eq(testDateFormatted), eq(testVendedorId), eq(null), eq(null))
    }

    @Test
    fun `cargarRutasParaFechaSeleccionada - FALLO - cuando no hay ID de vendedor`() = runTest {
        // Arrange
        whenever(sessionManager.getIdSeller()).thenReturn(null) // Vendedor no logueado

        // Act
        viewModel.cargarRutasParaFechaSeleccionada(defaultLat, defaultLon)

        // Assert
        assertEquals(false, viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
        assertEquals("Error: No se encontró ID de vendedor. Inicie sesión de nuevo.", viewModel.error.value)
        assertTrue(viewModel.rutas.value.isNullOrEmpty())
        verify(repository, never()).getRutasDelDia(any(), any(), any(), any())
    }

    @Test
    fun `cargarRutasParaFechaSeleccionada - FALLO - maneja IOException`() = runTest {
        // Arrange
        val errorMsg = "Error de red"
        whenever(repository.getRutasDelDia(eq(testDateFormatted), eq(testVendedorId), any(), any()))
            .thenThrow(IOException(errorMsg)) // Simula error de red

        // Act
        viewModel.cargarRutasParaFechaSeleccionada(null, null) // Intentamos cargar

        // Assert
        assertEquals(false, viewModel.isLoading.value)
        assertEquals("Error de conexión. Revisa tu red.", viewModel.error.value)
        assertTrue(viewModel.rutas.value.isNullOrEmpty())
    }
}