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
import com.medisupply.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var currentSelectedTab = R.id.nav_clientes

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        setupUI()
        setupBottomNavigation()

        // Mostrar el fragmento de Clientes por defecto
        if (savedInstanceState == null) {
            showFragment(ClientesFragment())
            updateTabSelection(R.id.nav_clientes)
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
        binding.navClientes.setOnClickListener {
            if (currentSelectedTab != R.id.nav_clientes) {
                showFragment(ClientesFragment())
                updateTabSelection(R.id.nav_clientes)
            }
        }

        binding.navInventario.setOnClickListener {
            if (currentSelectedTab != R.id.nav_inventario) {
                showFragment(InventarioFragment())
                updateTabSelection(R.id.nav_inventario)
            }
        }

        binding.navRutas.setOnClickListener {
            if (currentSelectedTab != R.id.nav_rutas) {
                showFragment(RutasFragment())
                updateTabSelection(R.id.nav_rutas)
            }
        }

        binding.navPedidos.setOnClickListener {
            if (currentSelectedTab != R.id.nav_pedidos) {
                showFragment(PedidosFragment())
                updateTabSelection(R.id.nav_pedidos)
            }
        }
    }

    private fun updateTabSelection(selectedTabId: Int) {
        currentSelectedTab = selectedTabId

        // Reset all to unselected state (blue text, normal icons)
        binding.textClientes.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))
        binding.textInventario.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))
        binding.textRutas.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))
        binding.textPedidos.setTextColor(resources.getColor(R.color.md_theme_light_primary, null))

        binding.iconClientes.setImageResource(R.drawable.clients)
        binding.iconInventario.setImageResource(R.drawable.inventario)
        binding.iconRutas.setImageResource(R.drawable.rutas)
        binding.iconPedidos.setImageResource(R.drawable.pedidos)

        // Set selected tab to black text and fill icon
        when (selectedTabId) {
            R.id.nav_clientes -> {
                binding.textClientes.setTextColor(resources.getColor(android.R.color.black, null))
                binding.iconClientes.setImageResource(R.drawable.clients_fill)
            }
            R.id.nav_inventario -> {
                binding.textInventario.setTextColor(resources.getColor(android.R.color.black, null))
                binding.iconInventario.setImageResource(R.drawable.inventario_fill)
            }
            R.id.nav_rutas -> {
                binding.textRutas.setTextColor(resources.getColor(android.R.color.black, null))
                binding.iconRutas.setImageResource(R.drawable.rutas_fill)
            }
            R.id.nav_pedidos -> {
                binding.textPedidos.setTextColor(resources.getColor(android.R.color.black, null))
                binding.iconPedidos.setImageResource(R.drawable.pedidos_fill)
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logout() {
        // Borrar datos de sesión y preferencias
        clearSessionData()

        // Navegar al LoginFragment
        val navController = findNavController()
        navController.navigate(R.id.action_homeFragment_to_loginFragment, null, NavOptions.Builder()
            .setPopUpTo(R.id.homeFragment, true)
            .build())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}