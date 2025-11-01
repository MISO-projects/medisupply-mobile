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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private val _fechaSeleccionada = MutableLiveData<Calendar>()
    val fechaSeleccionada: LiveData<Calendar> = _fechaSeleccionada

    // LiveData para el texto de la fecha
    private val _fechaFormateada = MutableLiveData<String>()
    val fechaFormateada: LiveData<String> = _fechaFormateada

    // --- ASEGÚRATE DE TENER AMBOS FORMATEADORES AQUÍ ---
    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    // Usamos Locale en español para el nombre del día y mes
    private val uiDateFormatter = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
    // --- FIN DE LA SECCIÓN IMPORTANTE ---


    init {
        // Cargar las rutas para el día actual al iniciar
        seleccionarFecha(Calendar.getInstance())
    }

    /**
     * Llama al ViewModel para cargar las rutas de la fecha seleccionada.
     */
    fun seleccionarFecha(fecha: Calendar) {
        _fechaSeleccionada.value = fecha
        _fechaFormateada.value = formatUiDate(fecha) // Actualiza el texto
        cargarRutas()
    }

    /**
     * Formatea la fecha para la UI, añadiendo "Hoy" si corresponde.
     */
    private fun formatUiDate(calendar: Calendar): String {
        val today = Calendar.getInstance()
        // Esta línea usa el 'uiDateFormatter'
        val uiText = uiDateFormatter.format(calendar.time).replaceFirstChar { it.uppercase() }

        return if (isSameDay(calendar, today)) {
            // Usamos la fecha actual (Sábado, 1 de noviembre) como ejemplo
            "Hoy ($uiText)"
        } else {
            uiText
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
                val vendedorId = "f4d5a8cd-5f3f-41eb-a0f3-28a3924c7f55"

                val fechaFormateada = _fechaSeleccionada.value?.let {
                    apiDateFormatter.format(it.time)
                } ?: return@launch

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