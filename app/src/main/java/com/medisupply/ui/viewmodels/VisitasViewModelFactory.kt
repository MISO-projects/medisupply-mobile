package com.medisupply.ui.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager

class VisitasViewModelFactory(
    private val application: Application,
    private val repository: VisitasRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VisitasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VisitasViewModel(application, repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}