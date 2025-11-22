package com.medisupply.viewmodels

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.R
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.ui.viewmodels.DetalleVisitaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class DetalleVisitaViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Usamos UnconfinedTestDispatcher para ejecución inmediata de corrutinas
    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var repository: VisitasRepository

    @Mock
    private lateinit var application: Application

    private lateinit var viewModel: DetalleVisitaViewModel

    private val testVisitaId = "visita-123"

    // CORREGIDO: Objeto dummy con TODOS los campos que pide el constructor ahora
    private val mockVisitaDetalle = VisitaDetalle(
        id = testVisitaId,
        clienteId = "c1",
        vendedorId = "v1",                // Nuevo campo requerido
        evidencia = null,
        nombreInstitucion = "Hospital Test",
        direccion = "Calle 123",
        clienteContacto = "Juan",
        fechaVisitaProgramada = "2025-11-22T10:00:00Z",
        inicio = null,                    // Nuevo campo requerido
        fin = null,                       // Nuevo campo requerido
        estado = "PENDIENTE",
        detalle = "Nota",
        createdAt = "2025-01-01T10:00:00Z", // Nuevo campo requerido
        updatedAt = "2025-01-01T10:00:00Z", // Nuevo campo requerido
        tiempoDesplazamiento = "10 min",
        notasVisitasAnteriores = emptyList(),
        productosPreferidos = emptyList()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        // Mock Application para devolver strings
        whenever(application.getString(R.string.error_conexion)).thenReturn("Error de conexión. Revisa tu red.")
        whenever(application.getString(eq(R.string.error_cargar_detalle), anyOrNull())).thenAnswer { 
            val message = it.arguments[1] as? String ?: ""
            "Error al cargar el detalle: $message"
        }
        whenever(application.getString(eq(R.string.error_cancelar_visita), anyOrNull())).thenAnswer {
            val message = it.arguments[1] as? String ?: ""
            "Error al cancelar la visita: $message"
        }
        whenever(application.applicationContext).thenReturn(application)
        
        viewModel = DetalleVisitaViewModel(application, repository, testVisitaId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadVisitaDetalle success updates liveData`() = runTest {
        // Given
        whenever(repository.getVisitaById(any(), anyOrNull(), anyOrNull())).thenReturn(mockVisitaDetalle)

        // When
        viewModel.loadVisitaDetalle(1.0, 1.0)

        // Then
        assertNotNull(viewModel.visita.value)
        assertEquals("Hospital Test", viewModel.visita.value?.nombreInstitucion)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `loadVisitaDetalle network error updates error liveData`() = runTest {
        // Given
        whenever(repository.getVisitaById(any(), anyOrNull(), anyOrNull())).thenAnswer {
            throw IOException("No connection")
        }

        // When
        viewModel.loadVisitaDetalle(1.0, 1.0)

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error de conexión") || viewModel.error.value!!.contains("Connection error"))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `loadVisitaDetalle generic error updates error liveData`() = runTest {
        // Given
        whenever(repository.getVisitaById(any(), anyOrNull(), anyOrNull())).thenAnswer {
            throw RuntimeException("Server error")
        }

        // When
        viewModel.loadVisitaDetalle(1.0, 1.0)

        // Then
        assertNotNull(viewModel.error.value)
        val errorMessage = application.getString(R.string.error_cargar_detalle, "Server error")
        assertTrue(viewModel.error.value!!.contains("Error al cargar el detalle") || viewModel.error.value!!.contains("Error loading details"))
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `cancelarVisita success updates cancelacionExitosa`() = runTest {
        // Given
        val motivo = "No estaba"
        // El repositorio devuelve una visita actualizada (con estado CANCELADA)
        val visitaCancelada = mockVisitaDetalle.copy(estado = "CANCELADA")

        // Configuramos el mock para que acepte cualquier string en los campos vacíos
        whenever(repository.registrarVisita(
            eq(testVisitaId), // ID debe coincidir
            eq(motivo),       // Detalle debe coincidir
            any(), any(), any(), // Campos vacíos (contacto, inicio, fin)
            eq("CANCELADA"),  // Estado debe ser CANCELADA
            anyOrNull()       // Archivo null
        )).thenReturn(visitaCancelada)

        // When
        viewModel.cancelarVisita(motivo)

        // Then
        assertEquals(true, viewModel.cancelacionExitosa.value)
        assertEquals(false, viewModel.isLoading.value)

        // Verificación extra: Asegurarse de que llamó al repositorio con los parámetros correctos
        verify(repository).registrarVisita(
            visitaId = testVisitaId,
            detalle = motivo,
            clienteContacto = "",
            inicio = "",
            fin = "",
            estado = "CANCELADA",
            archivoEvidencia = null
        )
    }

    @Test
    fun `cancelarVisita error updates error liveData`() = runTest {
        // Given
        whenever(repository.registrarVisita(
            any(), any(), any(), any(), any(), any(), anyOrNull()
        )).thenAnswer { throw RuntimeException("Failed to cancel") }

        // When
        viewModel.cancelarVisita("Motivo")

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error al cancelar") || viewModel.error.value!!.contains("Error canceling"))
        // No debe marcarse como exitosa (o debe ser false/null)
        assertNotEquals(true, viewModel.cancelacionExitosa.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `retry calls loadVisitaDetalle with default coordinates`() = runTest {
        // Given
        whenever(repository.getVisitaById(any(), anyOrNull(), anyOrNull())).thenReturn(mockVisitaDetalle)

        // When
        viewModel.retry()

        // Then
        // Verificamos que llame al repositorio con las coordenadas fijas que pusiste en el código
        verify(repository).getVisitaById(testVisitaId, 7.1384581600911945, -73.12422778151247)
        assertNotNull(viewModel.visita.value)
    }
}