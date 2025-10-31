package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.PedidoRepository

class ClientPedidosViewModelFactory(
    private val pedidoRepository: PedidoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientPedidosViewModel::class.java)) {
            return ClientPedidosViewModel(pedidoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

