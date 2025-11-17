package com.medisupply.ui.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.session.SessionManager
import com.medisupply.ui.viewmodels.DetalleVisitaViewModel
import com.medisupply.ui.viewmodels.VisitasViewModel

class DetalleVisitaViewModelFactory(
    private val repository: VisitasRepository,
    private val visitaId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetalleVisitaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetalleVisitaViewModel(repository, visitaId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}