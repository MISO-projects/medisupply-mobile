package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.InventarioRepository

/**
 * Factory para crear instancias de InventarioViewModel
 */
class InventarioViewModelFactory(
    private val inventarioRepository: InventarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(inventarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

