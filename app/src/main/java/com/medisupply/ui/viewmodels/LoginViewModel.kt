
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
    private val _navigationEvent = MutableLiveData<NavigationEvent>()
    val navigationEvent: LiveData<NavigationEvent> = _navigationEvent

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

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
        NetworkServiceAdapter.apiService.getMe(token).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val userProfile = response.body()!!

                    when (userProfile.role) {
                        "client" -> {
                            // Si el rol es 'client', navega a la pantalla del cliente
                            _navigationEvent.postValue(NavigationEvent.NavigateToClientHome)
                        }
                        "seller", null -> {
                            // Si el rol es 'seller' o no existe (null), navega a la pantalla del vendedor
                            _navigationEvent.postValue(NavigationEvent.NavigateToHome)
                        }
                        else -> {
                            // Para cualquier otro rol no esperado
                            _errorMessage.postValue("Rol no autorizado: ${userProfile.role}")
                        }
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
    object NavigateToClientHome : NavigationEvent() 
}