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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class RegistrarVisitaViewModel(
    private val repository: VisitasRepository,
    private val visitaId: String
) : ViewModel() {

    // Formateador para convertir la hora a ISO 8601 UTC
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // LiveData para el estado de la UI
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // LiveData para notificar el éxito (devuelve el objeto actualizado)
    private val _registroExitoso = MutableLiveData<VisitaDetalle?>()
    val registroExitoso: LiveData<VisitaDetalle?> = _registroExitoso

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
                // 1. Formatear las horas a ISO 8601 UTC (usando la fecha de HOY)
                val inicioISO = convertirHoraAISO(horaInicio)
                val finISO = convertirHoraAISO(horaFin)

                // 2. Crear el objeto de la petición
                val requestBody = RegistroVisitaRequest(
                    detalle = detalle,
                    clienteContacto = contacto,
                    inicio = inicioISO,
                    fin = finISO,
                    evidencia = "https://mi-storage.com/fotos/visita-dummy.jpg", // Dato dummy
                    estado = "REALIZADA" // Dato fijo
                )

                // 3. Llamar al repositorio y recibir la visita actualizada
                val visitaActualizada = repository.registrarVisita(visitaId, requestBody)

                // 4. Notificar éxito enviando el objeto
                _registroExitoso.value = visitaActualizada

            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido al guardar"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Convierte una hora simple (ej: "14:30") a un string ISO 8601 UTC
     * usando la fecha de HOY.
     */
    private fun convertirHoraAISO(horaHHmm: String): String {
        val (hora, minuto) = horaHHmm.split(":").map { it.toInt() }

        // Usamos la fecha de HOY
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hora)
        calendar.set(Calendar.MINUTE, minuto)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Formateamos a UTC
        return isoFormatter.format(calendar.time)
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