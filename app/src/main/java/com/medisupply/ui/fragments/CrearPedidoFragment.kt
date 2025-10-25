package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.models.Cliente
import com.medisupply.data.repositories.ClienteRepository
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.data.session.SessionManager
import com.medisupply.databinding.FragmentCrearPedidoBinding
import com.medisupply.ui.adapters.ProductoSearchAdapter
import com.medisupply.ui.adapters.ProductosPedidoAdapter
import com.medisupply.ui.viewmodels.CrearPedidoViewModel
import com.medisupply.ui.viewmodels.CrearPedidoViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrearPedidoFragment : Fragment() {

    private var _binding: FragmentCrearPedidoBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchResultsAdapter: ProductoSearchAdapter
    private lateinit var selectedProductosAdapter: ProductosPedidoAdapter
    private lateinit var sessionManager: SessionManager
    private var clientesList: List<Cliente> = emptyList()
    private var selectedCliente: Cliente? = null
    private var searchJob: Job? = null

    private val viewModel: CrearPedidoViewModel by viewModels {
        val apiService = NetworkServiceAdapter.getInstance().create(ApiService::class.java)
        CrearPedidoViewModelFactory(
            ClienteRepository(apiService),
            InventarioRepository(apiService),
            PedidoRepository(apiService)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_crear_pedido,
            container,
            false
        )

        sessionManager = SessionManager(requireContext())
        setupUI()
        setupObservers()

        return binding.root
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
        viewModel.clientes.observe(viewLifecycleOwner) { clientes ->
            clientesList = clientes
            setupInstitutionDropdown(clientes)
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { productos ->
            searchResultsAdapter.submitList(productos)
            binding.searchResultsRecyclerView?.isVisible = productos.isNotEmpty()
        }

        viewModel.selectedProductos.observe(viewLifecycleOwner) { productos ->
            selectedProductosAdapter.submitList(productos)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar?.isVisible = isLoading
            binding.btnCrearPedido.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.pedidoCreado.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                Toast.makeText(
                    requireContext(),
                    "Pedido creado exitosamente: ${response.numeroPedido}",
                    Toast.LENGTH_LONG
                ).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupInstitutionDropdown(clientes: List<Cliente>) {
        val clienteNames = clientes.map { it.nombre }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            clienteNames
        )

        binding.institutionAutocomplete.setAdapter(adapter)
        
        binding.institutionAutocomplete.setOnItemClickListener { _, _, position, _ ->
            selectedCliente = clientes[position]
        }
    }

    private fun setupProductsList() {
        searchResultsAdapter = ProductoSearchAdapter { producto ->
            viewModel.addProducto(producto)
            searchJob?.cancel()
            binding.searchProductoText.setText("")
            searchResultsAdapter.submitList(emptyList())
            binding.searchResultsRecyclerView?.isVisible = false
        }

        binding.searchResultsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultsAdapter
        }

        selectedProductosAdapter = ProductosPedidoAdapter { productoConCantidad ->
            viewModel.updateProductoCantidad(productoConCantidad)
        }

        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = selectedProductosAdapter
        }
    }

    private fun setupSearch() {
        binding.searchProductoText.addTextChangedListener { editable ->
            val query = editable?.toString()?.trim() ?: ""
            
            searchJob?.cancel()
            
            if (query.isNotEmpty()) {
                searchJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(300) // Debounce for 300ms
                    viewModel.searchProductos(query)
                }
            } else {
                searchResultsAdapter.submitList(emptyList())
                binding.searchResultsRecyclerView?.isVisible = false
            }
        }
    }

    private fun createOrder() {
        val institution = binding.institutionAutocomplete.text.toString()
        val notes = binding.notesEditText.text.toString()

        if (institution.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Por favor seleccione una institución",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (selectedCliente == null) {
            Toast.makeText(
                requireContext(),
                "Por favor seleccione una institución válida",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val userName = sessionManager.getUserName() ?: "admin"
        val vendedorId = "vendedor-1"

        viewModel.crearPedido(
            clienteId = selectedCliente!!.id,
            vendedorId = vendedorId,
            creadoPor = userName,
            observaciones = notes.ifBlank { null }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
