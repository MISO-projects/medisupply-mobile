package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.ClienteRepository

/**
 * Factory para crear instancias de ClientesViewModel
 */
class ClientesViewModelFactory(
    private val clienteRepository: ClienteRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientesViewModel::class.java)) {
            return ClientesViewModel(clienteRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
