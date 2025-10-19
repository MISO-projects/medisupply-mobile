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
import android.widget.TextView
import com.medisupply.R
import com.medisupply.data.models.RegisterRequest
import com.medisupply.ui.viewmodels.RegisterViewModel
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a las vistas
        val institutionName = view.findViewById<TextInputEditText>(R.id.institution_name_edit_text)
        val nit = view.findViewById<TextInputEditText>(R.id.nit_edit_text)
        val nitLayout = view.findViewById<TextInputLayout>(R.id.nit_layout)
        val address = view.findViewById<TextInputEditText>(R.id.address_edit_text)
        val responsibleName = view.findViewById<TextInputEditText>(R.id.responsible_name_edit_text)
        val email = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val emailLayout = view.findViewById<TextInputLayout>(R.id.email_layout)
        val password = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val passwordLayout = view.findViewById<TextInputLayout>(R.id.password_layout)
        val registerButton = view.findViewById<Button>(R.id.register_button)

        registerButton.setOnClickListener {
            val institutionNameValue = institutionName.text.toString()
            val nitValue = nit.text.toString()
            val addressValue = address.text.toString()
            val responsibleNameValue = responsibleName.text.toString()
            val emailValue = email.text.toString()
            val passwordValue = password.text.toString()

            val isNitValid = isValidNIT(nitValue)
            val isEmailValid = isValidEmail(emailValue)
            val isPasswordValid = isValidPassword(passwordValue)

            if (isNitValid && isEmailValid && isPasswordValid) {
                val registerRequest = RegisterRequest(
                    email = emailValue,
                    username = responsibleNameValue,
                    role = "client",
                    password = passwordValue,
                    nombre = institutionNameValue,
                    nit = nitValue,
                    logoUrl = "https://storage.googleapis.com/logos/hospital-general.png",
                    address = addressValue
                )

                registerViewModel.register(registerRequest)
            } else {
                if (!isNitValid) {
                    nitLayout.error = "El NIT debe contener solo números"
                } else {
                    nitLayout.error = null
                }
                if (!isEmailValid) {
                    emailLayout.error = "Ingrese un correo electrónico válido"
                } else {
                    emailLayout.error = null
                }
                if (!isPasswordValid) {
                    passwordLayout.error = "La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una letra minúscula y un número"
                } else {
                    passwordLayout.error = null
                }
            }
        }

        registerViewModel.registrationStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Registro exitoso", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        }

        // Opcional: Agregar navegación para el texto "Inicia sesión"
        val goToLogin = view.findViewById<TextView>(R.id.go_to_login_text_view)
        goToLogin.setOnClickListener {
            findNavController().navigateUp() // Vuelve a la pantalla anterior (Login)
        }
    }

    private fun isValidNIT(nit: String): Boolean {
        return nit.matches(Regex("^\\d+$"))
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        // Ejemplo de validación: mínimo 8 caracteres, al menos una letra mayúscula, una letra minúscula y un número
        return password.matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"))
    }
}