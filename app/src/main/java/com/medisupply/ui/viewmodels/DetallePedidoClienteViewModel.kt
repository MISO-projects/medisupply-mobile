package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.PedidoDetalle
import com.medisupply.data.repositories.PedidoRepository
import kotlinx.coroutines.launch

class DetallePedidoClienteViewModel(
    private val pedidoRepository: PedidoRepository,
    private val pedidoId: String
) : ViewModel() {

    private val _pedido = MutableLiveData<PedidoDetalle?>()
    val pedido: LiveData<PedidoDetalle?> = _pedido

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadPedidoDetails()
    }

    fun loadPedidoDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val pedidoDetails = pedidoRepository.obtenerPedidoPorId(pedidoId)
                _pedido.value = pedidoDetails

            } catch (e: Exception) {
                _error.value = "Error al cargar detalles del pedido: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry() {
        loadPedidoDetails()
    }
}

