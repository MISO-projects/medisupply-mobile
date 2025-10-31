package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.InventarioRepository

class CrearPedidoClienteViewModelFactory(
    private val inventarioRepository: InventarioRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CrearPedidoClienteViewModel::class.java)) {
            return CrearPedidoClienteViewModel(inventarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

