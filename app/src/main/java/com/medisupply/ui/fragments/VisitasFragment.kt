package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentVisitasBinding
import com.medisupply.ui.adapters.VisitasAdapter
import com.medisupply.ui.viewmodels.VisitasViewModel
import com.medisupply.ui.viewmodels.VisitasViewModelFactory
class VisitasFragment : Fragment() {

    private var _binding: FragmentVisitasBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: VisitasViewModel
    private lateinit var visitasAdapter: VisitasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_visitas,
            container,
            false
        )

        setupViewModel()
        setupRecyclerView()
        observeViewModel()

        return binding.root
    }

    private fun setupViewModel() {
        val application = requireActivity().application
        val apiService = NetworkServiceAdapter.getApiService()
        val repository = VisitasRepository(apiService)
        val factory = VisitasViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[VisitasViewModel::class.java]
    }

    private fun setupRecyclerView() {
        visitasAdapter = VisitasAdapter { visita ->
            // TODO: Manejar clic en la visita (ej. navegar al detalle)
        }

        binding.visitasRecyclerView.apply {
            adapter = visitasAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeViewModel() {
        // Observar lista de rutas (sin cambios)
        viewModel.rutas.observe(viewLifecycleOwner) { rutas ->
            visitasAdapter.submitList(rutas)
            binding.emptyView.isVisible = rutas.isEmpty() &&
                    !binding.loadingProgressBar.isVisible &&
                    !binding.errorView.isVisible
        }

        // Observar estado de carga (sin cambios)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            binding.visitasRecyclerView.isVisible = !isLoading
            binding.errorView.isVisible = false
            binding.emptyView.isVisible = false
        }

        // Observar errores (sin cambios)
        viewModel.error.observe(viewLifecycleOwner) { error ->
            val isError = error != null
            binding.errorView.isVisible = isError
            if (isError) {
                binding.errorText.text = error
                binding.visitasRecyclerView.isVisible = false
                binding.emptyView.isVisible = false
            }
        }


        // Botón de reintentar (sin cambios)
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}