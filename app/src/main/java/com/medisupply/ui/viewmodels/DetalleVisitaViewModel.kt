package com.medisupply.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medisupply.R
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import kotlinx.coroutines.launch
import java.io.IOException

class DetalleVisitaViewModel(
    application: Application,
    private val repository: VisitasRepository,
    private val visitaId: String
) : AndroidViewModel(application) {

    private val _visita = MutableLiveData<VisitaDetalle?>()
    val visita: LiveData<VisitaDetalle?> = _visita

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _cancelacionExitosa = MutableLiveData<Boolean>()
    val cancelacionExitosa: LiveData<Boolean> = _cancelacionExitosa

    /**
     * Carga el detalle de la visita, pasando la ubicación actual.
     */
    fun loadVisitaDetalle(lat: Double?, lon: Double?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = repository.getVisitaById(visitaId, lat, lon)
                _visita.value = result
            } catch (e: IOException) {
                _error.value = getApplication<Application>().getString(R.string.error_conexion)
            } catch (e: Exception) {
                _error.value = getApplication<Application>().getString(R.string.error_cargar_detalle, e.message ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cancela la visita.
     */
    fun cancelarVisita(motivo: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.registrarVisita(
                    visitaId = visitaId,
                    detalle = motivo,
                    clienteContacto = "",
                    inicio = "",
                    fin = "",
                    estado = "CANCELADA",
                    archivoEvidencia = null
                )
                _cancelacionExitosa.value = true
            } catch (e: Exception) {
                _error.value = getApplication<Application>().getString(R.string.error_cancelar_visita, e.message ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reintenta la carga (usado por el botón "Reintentar").
     * Usa la ubicación por defecto para el reintento.
     */
    fun retry() {
        loadVisitaDetalle(7.1384581600911945, -73.12422778151247)
    }
}

