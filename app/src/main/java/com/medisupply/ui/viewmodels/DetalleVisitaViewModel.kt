package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.RegistroVisitaRequest
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import kotlinx.coroutines.launch
import java.io.IOException

class DetalleVisitaViewModel(
    private val repository: VisitasRepository,
    private val visitaId: String
) : ViewModel() {

    private val _visita = MutableLiveData<VisitaDetalle?>()
    val visita: LiveData<VisitaDetalle?> = _visita

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _cancelacionExitosa = MutableLiveData<Boolean>()
    val cancelacionExitosa: LiveData<Boolean> = _cancelacionExitosa

    init {
        cargarDetalleVisita()
    }

    private fun cargarDetalleVisita() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val detalleVisita = repository.getVisitaById(visitaId)
                _visita.value = detalleVisita
            } catch (e: IOException) {
                _error.value = "Error de conexión. Revisa tu red."
            } catch (e: Exception) {
                _error.value = "Error al cargar el detalle: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        cargarDetalleVisita()
    }

    fun cancelarVisita(motivoConcatenado: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = RegistroVisitaRequest(
                    detalle = motivoConcatenado,
                    estado = "CANCELADA"
                )
                repository.registrarVisita(visitaId, request)
                _cancelacionExitosa.value = true

            } catch (e: Exception) {
                _error.value = "Error al cancelar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
