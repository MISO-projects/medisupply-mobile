package com.medisupply.ui.viewmodels

import androidx.lifecycle.ViewModel
import android.app.Application 
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager

class VisitasViewModelFactory(
    private val repository: VisitasRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pasar 'repository' y 'sessionManager'
            return VisitasViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}