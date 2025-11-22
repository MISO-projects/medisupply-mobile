package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import kotlinx.coroutines.launch
import java.io.File

class RegistrarVisitaViewModel(
    private val repository: VisitasRepository,
    private val visitaId: String
) : ViewModel() {

    // Variable para guardar temporalmente la foto seleccionada
    private var archivoSeleccionado: File? = null

    // LiveData para el estado de la UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData para notificar el éxito (devuelve el objeto actualizado)
    private val _registroExitoso = MutableLiveData<VisitaDetalle?>()
    val registroExitoso: LiveData<VisitaDetalle?> = _registroExitoso

    /**
     * Función para recibir el archivo desde el Fragmento
     */
    fun setArchivoEvidencia(file: File?) {
        archivoSeleccionado = file
    }

    /**
     * Función principal llamada desde el Fragment para guardar la visita.
     */
    fun guardarVisita(
        detalle: String,
        contacto: String,
        horaInicio: String, // "10:30"
        horaFin: String    // "11:00"
    ) {
        // Reseteamos los estados
        _registroExitoso.value = null
        _error.value = null

        viewModelScope.launch {
            _isLoading.value = true
            try {

                val visitaActualizada = repository.registrarVisita(
                    visitaId = visitaId,
                    detalle = detalle,
                    clienteContacto = contacto,
                    inicio = horaInicio,
                    fin = horaFin, 
                    estado = "REALIZADA",
                    archivoEvidencia = archivoSeleccionado
                )

                // 4. Notificar éxito enviando el objeto
                _registroExitoso.value = visitaActualizada

            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido al guardar"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

// --- FACTORY ---

class RegistrarVisitaViewModelFactory(
    private val repository: VisitasRepository,
    private val visitaId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrarVisitaViewModel::class.java)) {
            return RegistrarVisitaViewModel(repository, visitaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}