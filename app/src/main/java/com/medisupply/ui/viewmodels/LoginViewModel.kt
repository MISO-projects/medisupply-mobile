
package com.medisupply.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.medisupply.data.models.LoginRequest
import com.medisupply.data.models.LoginResponse
import com.medisupply.data.models.UserProfileResponse
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    // LiveData para comunicar eventos de navegación al Fragment
    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    // LiveData para mostrar errores
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        // 1. Primera llamada: Login
        NetworkServiceAdapter.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val accessToken = response.body()!!.accessToken
                    // Si el login fue exitoso, obtenemos el perfil del usuario
                    getUserProfile("Bearer $accessToken")
                } else {
                    _errorMessage.postValue("Credenciales incorrectas. Código: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _errorMessage.postValue("Error de red: ${t.message}")
            }
        })
    }

    private fun getUserProfile(token: String) {
        // 2. Segunda llamada: Obtener perfil
        NetworkServiceAdapter.apiService.getMe(token).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val userProfile = response.body()!!
                    // 3. Lógica de navegación
                    if (userProfile.role == null || userProfile.role == "seller") {
                        _navigationEvent.postValue(NavigationEvent.NavigateToHome)
                    } else {
                        // Aquí puedes manejar otros roles si es necesario
                        _errorMessage.postValue("Rol no autorizado: ${userProfile.role}")
                    }
                } else {
                    _errorMessage.postValue("Error al obtener perfil. Código: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                _errorMessage.postValue("Error de red al obtener perfil: ${t.message}")
            }
        })
    }
}

// Clase sellada para manejar los eventos de navegación de forma segura
sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
}