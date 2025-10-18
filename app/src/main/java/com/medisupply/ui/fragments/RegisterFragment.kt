package com.medisupply.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import com.medisupply.R

class RegisterFragment : Fragment() {

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
        val address = view.findViewById<TextInputEditText>(R.id.address_edit_text)
        val responsibleName = view.findViewById<TextInputEditText>(R.id.responsible_name_edit_text)
        val email = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val password = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val registerButton = view.findViewById<Button>(R.id.register_button)

        registerButton.setOnClickListener {
            // Recolectar y imprimir la información
            println("--- Nuevo Registro ---")
            println("Institución: ${institutionName.text.toString()}")
            println("NIT: ${nit.text.toString()}")
            println("Dirección: ${address.text.toString()}")
            println("Responsable: ${responsibleName.text.toString()}")
            println("Email: ${email.text.toString()}")
            println("Contraseña: ${password.text.toString()}")
            println("----------------------")
        }

        // Opcional: Agregar navegación para el texto "Inicia sesión"
        val goToLogin = view.findViewById<TextView>(R.id.go_to_login_text_view)
        goToLogin.setOnClickListener {
            findNavController().navigateUp() // Vuelve a la pantalla anterior (Login)
        }
    }
}