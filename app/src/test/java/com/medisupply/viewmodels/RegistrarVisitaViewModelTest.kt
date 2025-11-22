package com.medisupply.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.ui.viewmodels.RegistrarVisitaViewModel
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
import java.io.File
import java.io.IOException

@ExperimentalCoroutinesApi
class RegistrarVisitaViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Usamos UnconfinedTestDispatcher para ejecución inmediata (evita problemas de sincronización)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Mock
    private lateinit var repository: VisitasRepository

    @Mock
    private lateinit var mockFile: File

    private lateinit var viewModel: RegistrarVisitaViewModel

    private val testVisitaId = "visita-456"

    // Objeto dummy de respuesta exitosa (con todos los campos requeridos)
    private val mockVisitaResponse = VisitaDetalle(
        id = testVisitaId,
        clienteId = "c1",
        vendedorId = "v1",
        evidencia = "https://foto.jpg",
        nombreInstitucion = "Hospital Test",
        direccion = "Calle 123",
        clienteContacto = "Juan",
        fechaVisitaProgramada = "2025-11-22T10:00:00Z",
        inicio = "10:00",
        fin = "11:00",
        estado = "REALIZADA",
        detalle = "Visita completada",
        createdAt = "2025-11-22T10:00:00Z",
        updatedAt = "2025-11-22T11:00:00Z",
        tiempoDesplazamiento = "10 min",
        notasVisitasAnteriores = emptyList(),
        productosPreferidos = emptyList()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = RegistrarVisitaViewModel(repository, testVisitaId)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `guardarVisita success updates registroExitoso liveData`() = runTest {
        // Given
        val detalle = "Todo bien"
        val contacto = "Dra. Ana"
        val inicio = "08:00"
        val fin = "09:00"

        whenever(repository.registrarVisita(
            eq(testVisitaId),
            eq(detalle),
            eq(contacto),
            eq(inicio),
            eq(fin),
            eq("REALIZADA"),
            anyOrNull()
        )).thenReturn(mockVisitaResponse)

        // When
        viewModel.guardarVisita(detalle, contacto, inicio, fin)

        // Then
        assertNotNull(viewModel.registroExitoso.value)
        assertEquals("REALIZADA", viewModel.registroExitoso.value?.estado)
        assertNull(viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `guardarVisita with file should pass file to repository`() = runTest {
        // Given
        // Simulamos que el usuario seleccionó un archivo
        viewModel.setArchivoEvidencia(mockFile)
        
        whenever(repository.registrarVisita(
            any(), any(), any(), any(), any(), any(), anyOrNull()
        )).thenReturn(mockVisitaResponse)

        // When
        viewModel.guardarVisita("Detalle", "Contacto", "10:00", "11:00")

        // Then
        // Verificamos que el repositorio recibió el archivo 'mockFile'
        verify(repository).registrarVisita(
            eq(testVisitaId),
            any(),
            any(),
            any(),
            any(),
            eq("REALIZADA"),
            eq(mockFile) // Aquí validamos que pasó el archivo
        )
    }

    @Test
    fun `guardarVisita failure updates error liveData`() = runTest {
        // Given
        whenever(repository.registrarVisita(
            any(), any(), any(), any(), any(), any(), anyOrNull()
        )).thenAnswer {
            throw IOException("Error de red al guardar")
        }

        // When
        viewModel.guardarVisita("Detalle", "Contacto", "10:00", "11:00")

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error de red"))
        assertNull(viewModel.registroExitoso.value)
        assertEquals(false, viewModel.isLoading.value)
    }

    @Test
    fun `guardarVisita handles generic exception`() = runTest {
        // Given
        whenever(repository.registrarVisita(
            any(), any(), any(), any(), any(), any(), anyOrNull()
        )).thenAnswer {
            throw RuntimeException("Error inesperado")
        }

        // When
        viewModel.guardarVisita("Detalle", "Contacto", "10:00", "11:00")

        // Then
        assertNotNull(viewModel.error.value)
        assertTrue(viewModel.error.value!!.contains("Error inesperado"))
        assertEquals(false, viewModel.isLoading.value)
    }
}