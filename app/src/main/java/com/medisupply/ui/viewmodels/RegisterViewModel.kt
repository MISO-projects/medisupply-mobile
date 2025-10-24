package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.ClienteRequest
import com.medisupply.data.models.RegisterRequest
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import kotlinx.coroutines.launch
class RegisterViewModel : ViewModel() {
    private val _registrationStatus = MutableLiveData<Boolean>()
    val registrationStatus: LiveData<Boolean> = _registrationStatus

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            try {
                val clienteRequest = ClienteRequest(
                    nombre = registerRequest.nombre,
                    nit = registerRequest.nit,
                    logoUrl = registerRequest.logoUrl,
                    address = registerRequest.address
                )
                val clienteResponse = NetworkServiceAdapter.getApiService().crearCliente(clienteRequest)

                if (clienteResponse.isSuccessful) {
                    val clienteBody = clienteResponse.body()
                    if (clienteBody != null) {
                        val registerRequestWithClient = registerRequest.copy(
                            id_client = clienteBody.id
                        )
                        val userResponse = NetworkServiceAdapter.getApiService().register(registerRequestWithClient)
                        _registrationStatus.postValue(userResponse.isSuccessful)
                    } else {
                        _registrationStatus.postValue(false)
                    }
                } else {
                    _registrationStatus.postValue(false)
                }
            } catch (e: Exception) {
                _registrationStatus.postValue(false)
            }
        }
    }
}