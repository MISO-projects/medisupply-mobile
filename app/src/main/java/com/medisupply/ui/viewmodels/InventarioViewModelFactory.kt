package com.medisupply.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.InventarioRepository

/**
 * Factory para crear instancias de InventarioViewModel
 */
class InventarioViewModelFactory(
    private val application: Application,
    private val inventarioRepository: InventarioRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventarioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventarioViewModel(application, inventarioRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

