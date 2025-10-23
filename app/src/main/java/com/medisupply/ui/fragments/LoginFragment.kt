package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.medisupply.R
import com.medisupply.ui.viewmodels.LoginViewModel
import com.medisupply.ui.viewmodels.LoginViewModelFactory
import com.medisupply.ui.viewmodels.NavigationEvent

class LoginFragment : Fragment() {

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailInputLayout = view.findViewById<TextInputLayout>(R.id.email_input_layout)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = view.findViewById<Button>(R.id.login_button)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (isValidEmail(email)) {
                loginViewModel.loginUser(email, password)
            } else {
                emailInputLayout.error = "Ingrese un correo electrónico válido"
            }
        }

        return view
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()

        val createAccount = view.findViewById<TextView>(R.id.create_account_text_view)
        createAccount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        
    }

    private fun observeViewModel() {
        loginViewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigationEvent.NavigateToHome -> {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is NavigationEvent.NavigateToClientHome -> {
                    findNavController().navigate(R.id.action_loginFragment_to_clientHomeFragment)
                }
            }
        }

        loginViewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}