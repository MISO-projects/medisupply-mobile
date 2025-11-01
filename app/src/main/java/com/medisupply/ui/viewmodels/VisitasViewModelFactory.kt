package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.PedidoRepository

class VisitasViewModelFactory(
    private val application: Application,
    private val repository: VisitasRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitasViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}