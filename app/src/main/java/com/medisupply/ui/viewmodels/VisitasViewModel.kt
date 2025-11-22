package com.medisupply.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medisupply.R
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VisitasViewModel(
    application: Application,
    private val repository: VisitasRepository,
    private val sessionManager: SessionManager
) : AndroidViewModel(application) {
    private val _rutas = MutableLiveData<List<RutaVisitaItem>>()
    val rutas: LiveData<List<RutaVisitaItem>> = _rutas
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _selectedDate = MutableLiveData<Date>()
    val selectedDate: LiveData<Date> = _selectedDate

    private val apiDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        _selectedDate.value = Calendar.getInstance().time
    }

    /**
     * Función pública para que el Fragment actualice la fecha.
     */
    fun seleccionarFecha(date: Date) {
        _selectedDate.value = date
    }

    /**
     * Función que carga rutas. Acepta lat/lon opcionales.
     */
    fun cargarRutasParaFechaSeleccionada(lat: Double?, lon: Double?) {
        val dateToLoad = _selectedDate.value ?: return // No hacer nada si no hay fecha

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                //Obtener el ID del vendedor
                val vendedorId = sessionManager.getIdSeller()
                if (vendedorId == null) {
                    _error.value = getApplication<Application>().getString(R.string.error_no_vendedor_id)
                    _isLoading.value = false
                    _rutas.value = emptyList()
                    return@launch
                }

                val fechaFormateada = apiDateFormatter.format(dateToLoad)
                val resultado = repository.getRutasDelDia(fechaFormateada, vendedorId, lat, lon)
                _rutas.value = resultado

            } catch (e: IOException) {
                _error.value = getApplication<Application>().getString(R.string.error_conexion)
                _rutas.value = emptyList()
            } catch (e: Exception) {
                _error.value = getApplication<Application>().getString(R.string.error_cargar_visitas, e.message ?: "")
                _rutas.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        cargarRutasParaFechaSeleccionada(null, null)
    }
}