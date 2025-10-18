// en /ui/fragments/LoginFragment.kt
package com.medisupply.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.medisupply.R

class LoginFragment : Fragment() {

    // Inyecta el ViewModel de forma segura
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = view.findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // La única responsabilidad del Fragment es delegar la acción al ViewModel
            loginViewModel.loginUser(email, password)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observamos los eventos del ViewModel para reaccionar
        observeViewModel()
    }

    private fun observeViewModel() {
        loginViewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigationEvent.NavigateToHome -> {
                    // Usamos Navigation Component para ir al HomeFragment
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
            }
        }

        loginViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            // Muestra un mensaje de error al usuario
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}