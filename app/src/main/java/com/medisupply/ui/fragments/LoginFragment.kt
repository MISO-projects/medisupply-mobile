package com.medisupply.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.medisupply.R
import com.medisupply.data.models.LoginRequest
import com.medisupply.data.models.LoginResponse
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = view.findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            println("Email: $email")
            println("Password: $password")

            loginUser(email, password)
        }

        return view
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        NetworkServiceAdapter.apiService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    // Imprimir la respuesta en el logcat
                    println("LoginResponse: $loginResponse")
                    // Manejar la respuesta exitosa de inicio de sesión
                    // Ejemplo: Guardar el token de autenticación, redirigir a la pantalla principal, etc.
                } else {
                    // Imprimir el código de error y el mensaje de error en el logcat
                    val errorCode = response.code()
                    val errorMessage = response.message()
                    println("Error en la respuesta de la API: Código $errorCode - Mensaje: $errorMessage")
                    // Manejar el caso de error en la respuesta de la API
                    // Ejemplo: Mostrar un mensaje de error al usuario
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                // Imprimir el mensaje de error en el logcat
                println("Error en la solicitud de red: ${t.message}")
                // Manejar el caso de error en la solicitud de red
                // Ejemplo: Mostrar un mensaje de error al usuario
            }
        })
    }
}