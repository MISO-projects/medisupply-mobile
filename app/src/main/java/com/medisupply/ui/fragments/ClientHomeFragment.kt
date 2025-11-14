package com.medisupply.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.medisupply.R
import com.medisupply.data.session.SessionManager
import com.medisupply.databinding.FragmentClientHomeBinding

class ClientHomeFragment : Fragment() {

    private var _binding: FragmentClientHomeBinding? = null
    private val binding get() = _binding!!
    private var currentSelectedTab = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_client_home,
            container,
            false
        )

        setupUI()
        setupBottomNavigation()

        // Por defecto, mostrar el welcome text sin fragmento cargado
        if (savedInstanceState == null) {
            binding.welcomeText.visibility = View.VISIBLE
            binding.fragmentContainer.visibility = View.GONE
        }

        return binding.root
    }

    private fun setupUI() {
        // Configurar eventos del header
        binding.logoutText.setOnClickListener {
            logout()
        }

        binding.menuIcon.setOnClickListener {
            // TODO: Implementar menú lateral
        }
    }

    private fun setupBottomNavigation() {
        binding.navInventario.setOnClickListener {
            if (currentSelectedTab != R.id.nav_inventario) {
                showFragment(InventarioFragment())
                updateTabSelection(R.id.nav_inventario)
            }
        }

        binding.navPedidos.setOnClickListener {
            if (currentSelectedTab != R.id.nav_pedidos) {
                showFragment(ClientPedidosFragment())
                updateTabSelection(R.id.nav_pedidos)
            }
        }

        binding.navEntrega.setOnClickListener {
            if (currentSelectedTab != R.id.nav_entrega) {
                showFragment(EntregasClienteFragment())
                updateTabSelection(R.id.nav_entrega)
            }
        }
    }

    private fun updateTabSelection(selectedTabId: Int) {
        currentSelectedTab = selectedTabId

        // Ocultar welcome text y mostrar fragment container
        binding.welcomeText.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE

        // Reset all to unselected state (blue text)
        binding.textInventario.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))
        binding.textPedidos.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))
        binding.textEntregas.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))

        // Set selected tab to black text
        when (selectedTabId) {
            R.id.nav_inventario -> {
                binding.textInventario.setTextColor(resources.getColor(android.R.color.black, null))
            }
            R.id.nav_pedidos -> {
                binding.textPedidos.setTextColor(resources.getColor(android.R.color.black, null))
            }
            R.id.nav_entrega -> {
                binding.textEntregas.setTextColor(resources.getColor(android.R.color.black, null))
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logout() {
        val sessionManager = SessionManager(requireContext())
        sessionManager.logout()

        val navController = findNavController()
        navController.navigate(
            R.id.action_clientHomeFragment_to_loginFragment,
            null,
            NavOptions.Builder()
                .setPopUpTo(R.id.clientHomeFragment, true)
                .build()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}