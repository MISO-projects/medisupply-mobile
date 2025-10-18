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
import androidx.lifecycle.ViewModelProvider
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
    
    private lateinit var viewModel: InventarioViewModel
    private lateinit var productosAdapter: ProductosAdapter

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

        setupViewModel()
        setupRecyclerView()
        setupSearchBar()
        setupFilterButtons()
        observeViewModel()

        return binding.root
    }

    private fun setupViewModel() {
        val apiService = NetworkServiceAdapter.apiService
        val repository = InventarioRepository(apiService)
        val factory = InventarioViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[InventarioViewModel::class.java]
    }

    private fun setupRecyclerView() {
        productosAdapter = ProductosAdapter { producto ->
            // TODO: Manejar click en producto si es necesario
        }

        binding.productosRecyclerView.apply {
            adapter = productosAdapter
            layoutManager = LinearLayoutManager(context)
        }
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
            getString(R.string.disponibles),
            getString(R.string.no_disponibles)
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
                        viewModel.filtrarPorDisponibilidad(true)
                        binding.btnDisponibilidad.text = getString(R.string.disponibles)
                    }
                    2 -> {
                        // No disponibles
                        viewModel.filtrarPorDisponibilidad(false)
                        binding.btnDisponibilidad.text = getString(R.string.no_disponibles)
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
            binding.loadingProgressBar.isVisible = isLoading
            binding.productosRecyclerView.isVisible = !isLoading
            binding.errorView.isVisible = false
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
