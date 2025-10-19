package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.RegisterRequest
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    private val _registrationStatus = MutableLiveData<Boolean>()
    val registrationStatus: LiveData<Boolean> = _registrationStatus

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            try {
                val response = NetworkServiceAdapter.apiService.register(registerRequest)
                _registrationStatus.postValue(response.isSuccessful)
            } catch (e: Exception) {
                _registrationStatus.postValue(false)
            }
        }
    }
}