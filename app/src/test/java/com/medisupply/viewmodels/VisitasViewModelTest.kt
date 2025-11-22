package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager
import com.medisupply.ui.viewmodels.VisitasViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Date

@ExperimentalCoroutinesApi
class VisitasViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // CAMBIO 1: Usamos UnconfinedTestDispatcher.
    // Esto hace que las corrutinas corran "ya mismo", eliminando la necesidad de advanceUntilIdle()
    // y evitando condiciones de carrera.
    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var repository: VisitasRepository

    @Mock
    private lateinit var sessionManager: SessionManager

    private lateinit var viewModel: VisitasViewModel

    private val mockRutas = listOf(
        RutaVisitaItem(
            id = "1",
            clienteId = "c1",
            nombre = "Hospital A",
            direccion = "Calle 123",
            horaDeLaCita = "08:00",
            estado = "PENDIENTE"
        ),
        RutaVisitaItem(
            id = "2",
            clienteId = "c2",
            nombre = "Clínica B",
            direccion = "Carrera 45",
            horaDeLaCita = "10:00",
            estado = "REALIZADA"
        )
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        viewModel = VisitasViewModel(repository, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cargarRutas should update rutas list on success`() = runTest {
        // Given
        val vendedorId = "vendedor-123"
        whenever(sessionManager.getIdSeller()).thenReturn(vendedorId)

        // CAMBIO 2: Usamos anyOrNull() para asegurar que acepte nulos en lat/lon
        whenever(repository.getRutasDelDia(any(), any(), anyOrNull(), anyOrNull())).thenReturn(mockRutas)

        // When
        viewModel.cargarRutasParaFechaSeleccionada(null, null)

        // Nota: Con UnconfinedTestDispatcher ya no necesitamos advanceUntilIdle()

        // Then
        assertNotNull("La lista de rutas no debería ser nula", viewModel.rutas.value)
        assertEquals(2, viewModel.rutas.value?.size)
        assertEquals("Hospital A", viewModel.rutas.value?.get(0)?.nombre)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `cargarRutas should show error if session id is missing`() = runTest {
        // Given
        whenever(sessionManager.getIdSeller()).thenReturn(null)

        // When
        viewModel.cargarRutasParaFechaSeleccionada(null, null)

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("No se encontró ID de vendedor"))
        // Verificamos que limpie la lista
        assertEquals(0, viewModel.rutas.value?.size ?: 0)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `cargarRutas should handle network error`() = runTest {
        // Given
        whenever(sessionManager.getIdSeller()).thenReturn("vendedor-123")

        // CAMBIO 3: Simulamos el error con thenAnswer para evitar validaciones estrictas de Mockito
        // Y usamos anyOrNull() para garantizar que el mock se active con los nulos
        whenever(repository.getRutasDelDia(any(), any(), anyOrNull(), anyOrNull())).thenAnswer {
            throw IOException("No internet")
        }

        // When
        viewModel.cargarRutasParaFechaSeleccionada(null, null)

        // Then
        assertNotNull("El error debería haber sido capturado", viewModel.error.value)
        assertTrue("El mensaje debe ser de conexión", viewModel.error.value!!.contains("Error de conexión"))
        assertEquals(0, viewModel.rutas.value?.size ?: 0)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `cargarRutas should handle generic exception`() = runTest {
        // Given
        val errorMsg = "Database crash"
        whenever(sessionManager.getIdSeller()).thenReturn("vendedor-123")

        whenever(repository.getRutasDelDia(any(), any(), anyOrNull(), anyOrNull())).thenAnswer {
            throw RuntimeException(errorMsg)
        }

        // When
        viewModel.cargarRutasParaFechaSeleccionada(null, null)

        // Then
        assertNotNull("El error genérico debería ser capturado", viewModel.error.value)
        assertTrue("El mensaje debe contener la excepción", viewModel.error.value!!.contains(errorMsg))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `seleccionarFecha should update selectedDate livedata`() {
        // Given
        val nuevaFecha = Date()

        // When
        viewModel.seleccionarFecha(nuevaFecha)

        // Then
        assertEquals(nuevaFecha, viewModel.selectedDate.value)
    }

    @Test
    fun `retry should call load with current state`() = runTest {
        // Given
        whenever(sessionManager.getIdSeller()).thenReturn("vendedor-123")
        whenever(repository.getRutasDelDia(any(), any(), anyOrNull(), anyOrNull())).thenReturn(mockRutas)

        // When
        viewModel.retry()

        // Then
        assertNotNull(viewModel.rutas.value)
        assertEquals(2, viewModel.rutas.value?.size)
    }
}