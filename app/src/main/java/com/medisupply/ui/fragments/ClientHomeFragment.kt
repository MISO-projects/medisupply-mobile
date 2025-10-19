package com.medisupply.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.medisupply.R

class ClientHomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_client_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutText = view.findViewById<TextView>(R.id.logout_text)
        logoutText.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Borrar datos de sesión y preferencias
        clearSessionData()

        // Navegar al LoginFragment
        val navController = findNavController()
        navController.navigate(R.id.action_clientHomeFragment_to_loginFragment)
    }

    private fun clearSessionData() {
        // Obtener referencia a las SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)

        // Borrar todos los datos de sesión
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}