package com.medisupply.repositories

import android.webkit.MimeTypeMap
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows
import retrofit2.Response
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
// CORRECCIÓN: Forzamos sdk = [34] para evitar el error de compatibilidad con SDK 35
@Config(manifest = Config.NONE, sdk = [34])
class VisitasRepositoryTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: VisitasRepository

    private val testDispatcher = StandardTestDispatcher()

    // Datos Dummy Actualizados
    private val mockVisitaDetalle = VisitaDetalle(
        id = "123",
        clienteId = "c1",
        vendedorId = "v1",
        nombreInstitucion = "Hospital Test",
        direccion = "Calle 1",
        clienteContacto = "Juan",
        fechaVisitaProgramada = "2025-01-01",
        inicio = null,
        fin = null,
        estado = "PENDIENTE",
        detalle = "Nota",
        evidencia = null,
        createdAt = "2025-01-01T08:00:00Z",
        updatedAt = "2025-01-01T08:00:00Z",
        tiempoDesplazamiento = null,
        notasVisitasAnteriores = emptyList(),
        productosPreferidos = emptyList()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        repository = VisitasRepository(apiService)

        // Configuración básica de Robolectric para MimeTypes
        val shadowMimeTypeMap = Shadows.shadowOf(MimeTypeMap.getSingleton())
        shadowMimeTypeMap.addExtensionMimeTypeMapping("jpg", "image/jpeg")
        shadowMimeTypeMap.addExtensionMimeTypeMapping("mp4", "video/mp4")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getRutasDelDia should return list from api`() = runTest {
        // Given
        val mockRutas = listOf(
            RutaVisitaItem("1", "c1", "Hosp A", "Dir 1", "08:00", "PENDIENTE")
        )
        whenever(apiService.getRutasDelDia(any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(mockRutas)

        // When
        val result = repository.getRutasDelDia("2025-01-01", "v1", 1.0, 1.0)

        // Then
        assertEquals(1, result.size)
        assertEquals("Hosp A", result[0].nombre)
        verify(apiService).getRutasDelDia("2025-01-01", "v1", 1.0, 1.0)
    }

    @Test
    fun `getVisitaById should return detail from api`() = runTest {
        // Given
        whenever(apiService.getVisitaById(any(), anyOrNull(), anyOrNull()))
            .thenReturn(mockVisitaDetalle)

        // When
        val result = repository.getVisitaById("123", 1.0, 1.0)

        // Then
        assertNotNull(result)
        assertEquals("Hospital Test", result.nombreInstitucion)
    }

    @Test
    fun `registrarVisita success without file`() = runTest {
        // Given
        val responseSuccess = Response.success(mockVisitaDetalle)

        whenever(apiService.registrarVisita(
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(responseSuccess)

        // When
        val result = repository.registrarVisita(
            visitaId = "123",
            detalle = "Nota final",
            clienteContacto = "Ana",
            inicio = "08:00",
            fin = "09:00",
            estado = "REALIZADA",
            archivoEvidencia = null
        )

        // Then
        assertNotNull(result)
        assertEquals("123", result.id)

        verify(apiService).registrarVisita(
            eq("123"),
            any(), any(), any(), any(), any(),
            eq(null)
        )
    }

    @Test
    fun `registrarVisita success with IMAGE file`() = runTest {
        // Given
        val mockFile = File("prueba.jpg")
        val responseSuccess = Response.success(mockVisitaDetalle)

        whenever(apiService.registrarVisita(
            any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(responseSuccess)

        // When
        repository.registrarVisita(
            "123", "Detalle", "Contacto", "08:00", "09:00", "REALIZADA", mockFile
        )

        // Then
        val captor = argumentCaptor<MultipartBody.Part>()

        verify(apiService).registrarVisita(
            any(), any(), any(), any(), any(), any(),
            captor.capture()
        )

        assertNotNull(captor.firstValue)
        assertTrue(captor.firstValue.body.contentType().toString().contains("image/jpeg"))
    }

    @Test
    fun `registrarVisita success with VIDEO file`() = runTest {
        // Given
        val mockFile = File("video_evidencia.mp4")
        val responseSuccess = Response.success(mockVisitaDetalle)

        whenever(apiService.registrarVisita(
            any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(responseSuccess)

        // When
        repository.registrarVisita(
            "123", "Detalle", "Contacto", "08:00", "09:00", "REALIZADA", mockFile
        )

        // Then
        val captor = argumentCaptor<MultipartBody.Part>()
        verify(apiService).registrarVisita(
            any(), any(), any(), any(), any(), any(),
            captor.capture()
        )

        assertNotNull(captor.firstValue)
        assertTrue(captor.firstValue.body.contentType().toString().contains("video/mp4"))
    }

    @Test
    fun `registrarVisita throws exception on API error`() = runTest {
        // Given
        val errorBody = "Error interno".toResponseBody("text/plain".toMediaTypeOrNull())
        val responseError = Response.error<VisitaDetalle>(500, errorBody)

        whenever(apiService.registrarVisita(
            any(), any(), any(), any(), any(), any(), anyOrNull()
        )).thenReturn(responseError)

        // When / Then
        try {
            repository.registrarVisita(
                "123", "A", "B", "C", "D", "E", null
            )
            fail("Debería haber lanzado una excepción")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Error al registrar visita"))
            assertTrue(e.message!!.contains("500"))
        }
    }
}