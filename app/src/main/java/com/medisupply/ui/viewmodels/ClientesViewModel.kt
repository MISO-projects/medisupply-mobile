package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.Cliente
import com.medisupply.data.repositories.ClienteRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Clientes
 */
class ClientesViewModel(private val clienteRepository: ClienteRepository) : ViewModel() {

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadClientes()
    }

    /**
     * Carga la lista de clientes
     */
    fun loadClientes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val clientesList = clienteRepository.getClientes()
                _clientes.value = clientesList
                
            } catch (e: Exception) {
                _error.value = "Error al cargar clientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reintenta cargar los clientes
     */
    fun retry() {
        loadClientes()
    }
}
