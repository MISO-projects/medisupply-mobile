package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.repositories.EntregasRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentEntregasClienteBinding
import com.medisupply.ui.adapters.EntregasProgramadasAdapter
import com.medisupply.ui.viewmodels.EntregasProgramadasViewModel
import com.medisupply.ui.viewmodels.EntregasProgramadasViewModelFactory

class EntregasClienteFragment : Fragment() {

    private var _binding: FragmentEntregasClienteBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: EntregasProgramadasAdapter

    private val viewModel: EntregasProgramadasViewModel by viewModels {
        EntregasProgramadasViewModelFactory(
            requireActivity().application,
            EntregasRepository(
                NetworkServiceAdapter.getApiService()
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
            R.layout.fragment_entregas_cliente,
            container,
            false
        )

        setupRecyclerView()
        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = EntregasProgramadasAdapter { entrega ->
            onEntregaClick(entrega)
        }

        binding.entregasRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EntregasClienteFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.entregas.observe(viewLifecycleOwner) { entregas ->
            adapter.submitList(entregas)
            updateUIState(entregas.isEmpty(), false, null)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            binding.swipeRefreshLayout.isRefreshing = isLoading
            if (isLoading) {
                binding.entregasRecyclerView.isVisible = false
                binding.errorView.isVisible = false
                binding.emptyView.isVisible = false
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

        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun updateUIState(isEmpty: Boolean, hasError: Boolean, error: String? = null) {
        binding.apply {
            entregasRecyclerView.isVisible = !isEmpty && !hasError
            emptyView.isVisible = isEmpty && !hasError
            errorView.isVisible = hasError
            loadingProgressBar.isVisible = false
        }
    }

    private fun onEntregaClick(entrega: com.medisupply.data.models.EntregaProgramadaItem) {
        val detailFragment = DetalleEntregaProgramadaFragment.newInstance(entrega)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

