package com.medisupply.viewmodels 
import com.medisupply.ui.viewmodels.VisitasViewModel
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.mockito.kotlin.willThrow
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@ExperimentalCoroutinesApi
class VisitasViewModelTest {

    // Regla para que LiveData funcione en tests unitarios
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Estas clases ahora serán encontradas gracias a los imports
    private lateinit var repository: VisitasRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: VisitasViewModel

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
        sessionManager = mock()
        viewModel = VisitasViewModel(repository, sessionManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun getTodayFormatted(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    }

    @Test
    fun `test init - EXITO - debe cargar rutas y ocultar loading`() = runTest {
        val vendedorId = "vendedor-123"
        val fecha = getTodayFormatted()
        // Esta clase ahora será encontrada
        val mockRutas = listOf(
            RutaVisitaItem("1", "c1", "Hospital", "Dir 1", "09:00", "PENDIENTE")
        )
        whenever(sessionManager.getIdSeller()).thenReturn(vendedorId)
        whenever(repository.getRutasDelDia(eq(fecha), eq(vendedorId))).thenReturn(mockRutas)

        viewModel.retry()

        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertEquals(mockRutas, viewModel.rutas.value)
        verify(repository).getRutasDelDia(eq(fecha), eq(vendedorId))
    }

    @Test
    fun `test init - FALLO - cuando no hay ID de vendedor`() = runTest {
        whenever(sessionManager.getIdSeller()).thenReturn(null)
        viewModel.retry()
        assertEquals(false, viewModel.isLoading.value)
        assertNotNull(viewModel.error.value)
        assertEquals("Error: No se encontró ID de vendedor. Inicie sesión de nuevo.", viewModel.error.value)
        assertTrue(viewModel.rutas.value.isNullOrEmpty())
        verify(repository, never()).getRutasDelDia(any(), any())
    }
}

