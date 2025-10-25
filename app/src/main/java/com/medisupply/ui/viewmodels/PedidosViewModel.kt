package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.Pedido
import com.medisupply.data.repositories.PedidoRepository
import kotlinx.coroutines.launch

class PedidosViewModel(private val pedidoRepository: PedidoRepository): ViewModel() {
    private val _pedidos = MutableLiveData<List<Pedido>>()
    val pedidos : LiveData<List<Pedido>> = _pedidos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadPedidos()
    }

    fun loadPedidos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val pedidosList = pedidoRepository.listarPedidos()
                _pedidos.value = pedidosList

            } catch (e: Exception) {
                _error.value = "Error al cargar pedidos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry(){
        loadPedidos()
    }
}