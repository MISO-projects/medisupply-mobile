package com.medisupply.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.EntregasRepository

class EntregasProgramadasViewModelFactory(
    private val application: Application,
    private val entregasRepository: EntregasRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntregasProgramadasViewModel::class.java)) {
            return EntregasProgramadasViewModel(application, entregasRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

