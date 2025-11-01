package com.medisupply.ui.viewmodels

// QUITA: import android.app.Application
// QUITA: import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel // <-- CAMBIO: Importar ViewModel normal
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager // <-- AÑADIDO: Importar
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// --- CAMBIOS AL CONSTRUCTOR ---
class VisitasViewModel(
    private val repository: VisitasRepository,
    private val sessionManager: SessionManager // <-- 1. RECIBIRLO AQUÍ
) : ViewModel() { // <-- 2. HEREDAR DE ViewModel NORMAL


   
    private val _rutas = MutableLiveData<List<RutaVisitaItem>>()
    val rutas: LiveData<List<RutaVisitaItem>> = _rutas
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        cargarRutasDeHoy()
    }

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

                val fechaDeHoy = apiDateFormatter.format(Calendar.getInstance().time)
                val resultado = repository.getRutasDelDia(fechaDeHoy, vendedorId)
                _rutas.value = resultado

            } catch (e: IOException) {
                _error.value = "Error de conexión. Revisa tu red."
                _rutas.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Error al cargar las visitas: ${e.message}"
                _rutas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        cargarRutasDeHoy()
    }
}