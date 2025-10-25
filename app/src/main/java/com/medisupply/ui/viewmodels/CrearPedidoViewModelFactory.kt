package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.ClienteRepository
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.PedidoRepository

class CrearPedidoViewModelFactory(
    private val clienteRepository: ClienteRepository,
    private val inventarioRepository: InventarioRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CrearPedidoViewModel::class.java)) {
            return CrearPedidoViewModel(
                clienteRepository,
                inventarioRepository,
                pedidoRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}