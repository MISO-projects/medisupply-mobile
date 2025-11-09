package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.EntregaProgramadaItem
import com.medisupply.data.repositories.EntregasRepository
import kotlinx.coroutines.launch

class EntregasProgramadasViewModel(
    private val entregasRepository: EntregasRepository
) : ViewModel() {

    private val _entregas = MutableLiveData<List<EntregaProgramadaItem>>()
    val entregas: LiveData<List<EntregaProgramadaItem>> = _entregas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadEntregas()
    }

    fun loadEntregas(
        estadoParada: String? = null,
        estadoRuta: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val entregasList = entregasRepository.obtenerEntregasProgramadas(
                    estadoParada = estadoParada,
                    estadoRuta = estadoRuta,
                    page = page,
                    pageSize = pageSize
                )
                _entregas.value = entregasList

            } catch (e: Exception) {
                _error.value = "Error al cargar entregas: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        loadEntregas()
    }
}

