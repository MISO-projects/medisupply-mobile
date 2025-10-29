package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentClientPedidosBinding
import com.medisupply.ui.adapters.ClientPedidosAdapter
import com.medisupply.ui.viewmodels.ClientPedidosViewModel
import com.medisupply.ui.viewmodels.ClientPedidosViewModelFactory

class ClientPedidosFragment : Fragment() {

    private var _binding: FragmentClientPedidosBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: ClientPedidosAdapter

    private val viewModel: ClientPedidosViewModel by viewModels {
        ClientPedidosViewModelFactory(
            PedidoRepository(
                NetworkServiceAdapter.getInstance().create(ApiService::class.java)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_client_pedidos,
            container,
            false
        )

        setupRecyclerView()
        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = ClientPedidosAdapter { pedido ->
            onPedidoClick(pedido)
        }

        binding.pedidosRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ClientPedidosFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.pedidos.observe(viewLifecycleOwner) { pedidos ->
            adapter.submitList(pedidos)
            updateUIState(pedidos.isEmpty(), false, null)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            binding.swipeRefreshLayout.isRefreshing = isLoading
            if (isLoading) {
                binding.pedidosRecyclerView.isVisible = false
                binding.errorView.isVisible = false
                binding.errorText.isVisible = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorText.text = error
                updateUIState(false, true, error)
            }
        }
    }

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.retry()
        }
        
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.md_theme_light_primary,
            R.color.md_theme_light_secondary
        )

        binding.crearPedidoButton.setOnClickListener {
            navigateToCrearPedido()
        }

        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun updateUIState(isEmpty: Boolean, hasError: Boolean, error: String? = null) {
        binding.apply {
            pedidosRecyclerView.isVisible = !isEmpty && !hasError
            emptyView.isVisible = isEmpty && !hasError
            errorView.isVisible = hasError
            loadingProgressBar.isVisible = false
        }
    }

    private fun onPedidoClick(pedido: com.medisupply.data.models.PedidoResumenCliente) {
        // Manejar el clic en el pedido
        Toast.makeText(
            requireContext(),
            "Pedido seleccionado: ${pedido.numeroPedido}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToCrearPedido() {
        val crearPedidoFragment = CrearPedidoClienteFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, crearPedidoFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

