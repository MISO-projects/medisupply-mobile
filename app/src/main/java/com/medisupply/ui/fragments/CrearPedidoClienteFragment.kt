package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentCrearPedidoClienteBinding
import com.medisupply.ui.adapters.ProductoSearchAdapter
import com.medisupply.ui.adapters.ProductosPedidoAdapter
import com.medisupply.ui.viewmodels.CrearPedidoClienteViewModel
import com.medisupply.ui.viewmodels.CrearPedidoClienteViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrearPedidoClienteFragment : Fragment() {

    private var _binding: FragmentCrearPedidoClienteBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchResultsAdapter: ProductoSearchAdapter
    private lateinit var selectedProductosAdapter: ProductosPedidoAdapter
    private var searchJob: Job? = null

    private val viewModel: CrearPedidoClienteViewModel by viewModels {
        val apiService = NetworkServiceAdapter.getInstance().create(ApiService::class.java)
        CrearPedidoClienteViewModelFactory(
            InventarioRepository(apiService)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_crear_pedido_cliente,
            container,
            false
        )

        setupUI()
        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Clear search results when returning to this fragment
        clearSearch()
    }

    private fun clearSearch() {
        binding.searchProductoText.setText("")
        searchResultsAdapter.submitList(emptyList())
        binding.searchResultsRecyclerView.isVisible = false
        searchJob?.cancel()
    }

    private fun setupUI() {
        // Setup products RecyclerView
        setupProductsList()

        // Setup create order button
        binding.btnCrearPedido.setOnClickListener {
            createOrder()
        }

        // Setup search functionality
        setupSearch()
    }

    private fun setupObservers() {
        viewModel.searchResults.observe(viewLifecycleOwner) { productos ->
            searchResultsAdapter.submitList(productos)
            binding.searchResultsRecyclerView.isVisible = productos.isNotEmpty()
        }

        viewModel.selectedProductos.observe(viewLifecycleOwner) { productos ->
            selectedProductosAdapter.submitList(productos)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            binding.btnCrearPedido.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        // Remove pedidoCreado observer since we navigate to summary instead
        // viewModel.pedidoCreado.observe(viewLifecycleOwner) { response ->
        //     if (response != null) {
        //         Toast.makeText(
        //             requireContext(),
        //             "Pedido creado exitosamente: ${response.numeroPedido}",
        //             Toast.LENGTH_LONG
        //         ).show()
        //         // Navigate back to pedidos list
        //         parentFragmentManager.popBackStack()
        //     }
        // }
    }

    private fun setupProductsList() {
        searchResultsAdapter = ProductoSearchAdapter { producto: com.medisupply.data.models.Producto ->
            viewModel.addProducto(producto)
            searchJob?.cancel()
            binding.searchProductoText.setText("")
            searchResultsAdapter.submitList(emptyList())
            binding.searchResultsRecyclerView.isVisible = false
        }

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultsAdapter
        }

        selectedProductosAdapter = ProductosPedidoAdapter { productoConCantidad: com.medisupply.ui.adapters.ProductoConCantidad ->
            viewModel.updateProductoCantidad(productoConCantidad)
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedProductosAdapter
        }
    }

    private fun setupSearch() {
        binding.searchProductoText.addTextChangedListener { editable: android.text.Editable? ->
            val query = editable?.toString()?.trim() ?: ""
            
            searchJob?.cancel()
            
            if (query.isNotEmpty()) {
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300) // Debounce for 300ms
                    viewModel.searchProductos(query)
                }
            } else {
                searchResultsAdapter.submitList(emptyList())
                binding.searchResultsRecyclerView.isVisible = false
            }
        }
    }

    private fun createOrder() {
        val notes = binding.notesEditText.text.toString()

        val selectedProducts = viewModel.selectedProductos.value?.filter { it.cantidad > 0 }
        if (selectedProducts.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "Por favor agregue al menos un producto",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val resumenFragment = ResumenPedidoClienteFragment.newInstance(
            productos = ArrayList(selectedProducts),
            observaciones = notes.ifBlank { null }
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, resumenFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

