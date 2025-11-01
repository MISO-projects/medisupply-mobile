package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class VisitasViewModel(private val repository: VisitasRepository) : ViewModel() {

    // LiveData para la lista de visitas
    private val _rutas = MutableLiveData<List<RutaVisitaItem>>()
    val rutas: LiveData<List<RutaVisitaItem>> = _rutas

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData para la fecha seleccionada
    private val _fechaSeleccionada = MutableLiveData<LocalDate>()
    val fechaSeleccionada: LiveData<LocalDate> = _fechaSeleccionada

    // Formateador de fecha para la API
    private val apiDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE // "YYYY-MM-DD"

    init {
        // Cargar las rutas para el día actual al iniciar
        seleccionarFecha(LocalDate.now())
    }

    /**
     * Llama al ViewModel para cargar las rutas de la fecha seleccionada.
     */
    fun seleccionarFecha(fecha: LocalDate) {
        _fechaSeleccionada.value = fecha
        cargarRutas()
    }

    /**
     * Función principal para obtener los datos del repositorio.
     */
    private fun cargarRutas() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // TODO: Obtener el ID del vendedor logueado (ej. SharedPreferences)
                // Usamos un ID harcodeado temporalmente, igual al de tu cURL
                val vendedorId = "f4d5a8cd-5f3f-41eb-a0f3-28a3924c7f55"

                val fechaFormateada = _fechaSeleccionada.value?.format(apiDateFormatter) ?: return@launch

                val resultado = repository.getRutasDelDia(fechaFormateada, vendedorId)
                _rutas.value = resultado

            } catch (e: IOException) { // Error de red
                _error.value = "Error de conexión. Revisa tu red."
                _rutas.value = emptyList() // Limpiar lista anterior
            } catch (e: Exception) { // Otros errores (ej. HTTP 404, 500)
                _error.value = "Error al cargar las visitas: ${e.message}"
                _rutas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Permite al usuario reintentar la carga de datos.
     */
    fun retry() {
        cargarRutas()
    }
}

/**
 * Factory para crear el VisitasViewModel con su repositorio.
 */
class VisitasViewModelFactory(
    private val repository: VisitasRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitasViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}