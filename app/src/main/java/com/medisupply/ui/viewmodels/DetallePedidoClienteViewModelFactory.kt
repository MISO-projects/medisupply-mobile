package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.PedidoRepository

class DetallePedidoClienteViewModelFactory(
    private val pedidoRepository: PedidoRepository,
    private val pedidoId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetallePedidoClienteViewModel::class.java)) {
            return DetallePedidoClienteViewModel(pedidoRepository, pedidoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

