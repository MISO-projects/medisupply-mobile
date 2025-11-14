package com.medisupply.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentInventarioBinding
import com.medisupply.ui.adapters.ProductosAdapter
import com.medisupply.ui.viewmodels.InventarioViewModel
import com.medisupply.ui.viewmodels.InventarioViewModelFactory

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var productosAdapter: ProductosAdapter
    private var hasAttemptedInitialLoad = false

    private val viewModel: InventarioViewModel by viewModels {
        val apiService = NetworkServiceAdapter.getApiService()
        val repository = InventarioRepository(apiService)
        InventarioViewModelFactory(
            requireContext().applicationContext as android.app.Application,
            repository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_inventario,
            container,
            false
        )

        setupRecyclerView()
        setupSearchBar()
        setupFilterButtons()
        observeViewModel()
        
        if (!hasAttemptedInitialLoad && !viewModel.hasDataLoaded()) {
            hasAttemptedInitialLoad = true
            viewModel.loadProductosIfNeeded()
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        productosAdapter = ProductosAdapter { producto ->
            val detailFragment = DetalleInventarioFragment.newInstance(producto)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.productosRecyclerView.apply {
            adapter = productosAdapter
            layoutManager = LinearLayoutManager(context)
        }
        
        // Setup SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.retry()
        }
        
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.md_theme_light_primary,
            R.color.md_theme_light_secondary
        )
    }

    private fun setupSearchBar() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                viewModel.buscarProductos(s?.toString() ?: "")
            }
        })
    }

    private fun setupFilterButtons() {
        // Botón de categorías
        binding.btnCategoria.setOnClickListener {
            showCategoriaDialog()
        }

        // Botón de disponibilidad
        binding.btnDisponibilidad.setOnClickListener {
            showDisponibilidadDialog()
        }
    }

    private fun showCategoriaDialog() {
        val categorias = viewModel.categorias.value ?: emptyList()
        val categoriasArray = arrayOf(getString(R.string.categoria_todos)) + categorias.toTypedArray()
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.categorias))
            .setItems(categoriasArray) { _, which ->
                if (which == 0) {
                    // Todas las categorías
                    viewModel.filtrarPorCategoria(null)
                    binding.btnCategoria.text = getString(R.string.categorias)
                } else {
                    // Categoría específica
                    val categoriaSeleccionada = categorias[which - 1]
                    viewModel.filtrarPorCategoria(categoriaSeleccionada)
                    binding.btnCategoria.text = categoriaSeleccionada
                }
            }
            .show()
    }

    private fun showDisponibilidadDialog() {
        val opciones = arrayOf(
            getString(R.string.todos),
            getString(R.string.disponible),
            getString(R.string.bloqueado),
            getString(R.string.en_revision)
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.disponibilidad))
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> {
                        // Todos
                        viewModel.filtrarPorDisponibilidad(null)
                        binding.btnDisponibilidad.text = getString(R.string.disponibilidad)
                    }
                    1 -> {
                        // Disponibles
                        viewModel.filtrarPorDisponibilidad("DISPONIBLE")
                        binding.btnDisponibilidad.text = getString(R.string.disponible)
                    }
                    2 -> {
                        // BLoequeados
                        viewModel.filtrarPorDisponibilidad("BLOQUEADO")
                        binding.btnDisponibilidad.text = getString(R.string.bloqueado)
                    }
                    3 -> {
                        // En revisión
                        viewModel.filtrarPorDisponibilidad("EN_REVISION")
                        binding.btnDisponibilidad.text = getString(R.string.en_revision)
                    }
                }
            }
            .show()
    }

    private fun observeViewModel() {
        // Observar productos filtrados
        viewModel.productosFiltrados.observe(viewLifecycleOwner) { productos ->
            productosAdapter.submitList(productos)
            
            // Mostrar vista vacía si no hay productos
            binding.emptyView.isVisible = productos.isEmpty() && 
                                         binding.loadingProgressBar.visibility == View.GONE &&
                                         binding.errorView.visibility == View.GONE
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                val hasData = viewModel.productosFiltrados.value?.isNotEmpty() == true
                if (hasData) {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.loadingProgressBar.isVisible = false
                } else {
                    binding.loadingProgressBar.isVisible = true
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } else {
                binding.loadingProgressBar.isVisible = false
                binding.swipeRefreshLayout.isRefreshing = false
                binding.productosRecyclerView.isVisible = true
            }
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorView.isVisible = true
                binding.errorText.text = error
                binding.productosRecyclerView.isVisible = false
                binding.loadingProgressBar.isVisible = false
            } else {
                binding.errorView.isVisible = false
                val productos = viewModel.productosFiltrados.value
                if (productos != null && productos.isNotEmpty()) {
                    binding.productosRecyclerView.isVisible = true
                }
            }
        }

        // Botón de reintentar
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
