package com.medisupply.ui.viewmodels

import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import com.medisupply.data.session.SessionManager

class VisitasViewModel(
    application: Application,
    private val repository: VisitasRepository
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application.applicationContext)

    // LiveData para la lista de visitas
    private val _rutas = MutableLiveData<List<RutaVisitaItem>>()
    val rutas: LiveData<List<RutaVisitaItem>> = _rutas

    // LiveData para el estado de carga
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData para errores
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        // Cargar las rutas de HOY al iniciar
        cargarRutasDeHoy()
    }

    /**
     * Función principal para obtener los datos del repositorio PARA HOY.
     */
    private fun cargarRutasDeHoy() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                //Obtener el ID del vendedor
                val vendedorId = sessionManager.getIdSeller()
                if (vendedorId == null) {
                    _error.value = "Error: No se encontró ID de vendedor. Inicie sesión de nuevo."
                    _isLoading.value = false
                    _rutas.value = emptyList()
                    return@launch
                }

                //Obtener la fecha de HOY
                val fechaDeHoy = apiDateFormatter.format(Calendar.getInstance().time)

                //Llamar al repositorio
                val resultado = repository.getRutasDelDia(fechaDeHoy, vendedorId)
                _rutas.value = resultado

            } catch (e: IOException) { // Error de red
                _error.value = "Error de conexión. Revisa tu red."
                _rutas.value = emptyList()
            } catch (e: Exception) { // Otros errores
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
        cargarRutasDeHoy()
    }
}

// --- La Factory (VisitasViewModelFactory) NO CAMBIA ---
// (Sigue siendo necesaria para inyectar Application y Repository)
