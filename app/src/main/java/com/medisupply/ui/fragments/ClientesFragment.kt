package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.models.Cliente
import com.medisupply.data.repositories.ClienteRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentClientesBinding
import com.medisupply.ui.adapters.ClientesAdapter
import com.medisupply.ui.viewmodels.ClientesViewModel
import com.medisupply.ui.viewmodels.ClientesViewModelFactory

class ClientesFragment : Fragment() {

    private var _binding: FragmentClientesBinding? = null
    private val binding get() = _binding!!

    private lateinit var clientesAdapter: ClientesAdapter
    
    // ViewModel con factory
    private val viewModel: ClientesViewModel by viewModels {
        ClientesViewModelFactory(
            ClienteRepository(
                NetworkServiceAdapter.getApiService()
            ),
            requireContext()
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_clientes,
            container,
            false
        )

        setupUI()
        setupObservers()

        return binding.root
    }

    private fun setupUI() {
        // Configurar RecyclerView
        clientesAdapter = ClientesAdapter { cliente ->
            onClienteClick(cliente)
        }
        
        binding.recyclerClientes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = clientesAdapter
        }

        // Configurar botón de reintentar
        binding.btnRetry.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun setupObservers() {
        // Observar lista de clientes
        viewModel.clientes.observe(viewLifecycleOwner) { clientes ->
            clientesAdapter.submitList(clientes)
            updateUIState(clientes.isEmpty(), false, null)
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            if (isLoading) {
                binding.recyclerClientes.isVisible = false
                binding.errorLayout.isVisible = false
                binding.emptyText.isVisible = false
            }
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorText.text = error
                updateUIState(false, true, error)
            }
        }
    }

    private fun updateUIState(isEmpty: Boolean, hasError: Boolean, error: String? = null) {
        binding.apply {
            recyclerClientes.isVisible = !isEmpty && !hasError
            emptyText.isVisible = isEmpty && !hasError
            errorLayout.isVisible = hasError
            progressBar.isVisible = false
        }
    }

    private fun onClienteClick(cliente: Cliente) {
        Toast.makeText(
            requireContext(), 
            getString(R.string.cliente_seleccionado, cliente.nombre), 
            Toast.LENGTH_SHORT
        ).show()
        // TODO: Navegar a detalle del cliente o realizar acción específica
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
