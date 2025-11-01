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
import java.time.LocalDate
import java.util.Calendar

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
        setupCalendar()
        observeViewModel()

        return binding.root
    }

    private fun setupViewModel() {
        // Asumiendo que NetworkServiceAdapter es tu singleton de Retrofit
        val apiService = NetworkServiceAdapter.getApiService()
        val repository = VisitasRepository(apiService)
        val factory = VisitasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[VisitasViewModel::class.java]
    }

    private fun setupRecyclerView() {
        visitasAdapter = VisitasAdapter { visita ->
            // TODO: Manejar clic en la visita (ej. navegar al detalle)
            // val action = VisitasFragmentDirections.actionVisitasFragmentToVisitaDetalleFragment(visita.id)
            // findNavController().navigate(action)
        }

        binding.visitasRecyclerView.apply {
            adapter = visitasAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // month es 0-indexado, por eso se suma 1
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            viewModel.seleccionarFecha(selectedDate)
        }
    }

    private fun observeViewModel() {
        // Observar lista de rutas
        viewModel.rutas.observe(viewLifecycleOwner) { rutas ->
            visitasAdapter.submitList(rutas)

            // Mostrar vista vacía solo si no está cargando y no hay error
            binding.emptyView.isVisible = rutas.isEmpty() &&
                    !binding.loadingProgressBar.isVisible &&
                    !binding.errorView.isVisible
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            // Ocultar RecyclerView, error y vacío mientras carga
            binding.visitasRecyclerView.isVisible = !isLoading
            binding.errorView.isVisible = false
            binding.emptyView.isVisible = false
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            val isError = error != null
            binding.errorView.isVisible = isError
            if (isError) {
                binding.errorText.text = error
                binding.visitasRecyclerView.isVisible = false
                binding.emptyView.isVisible = false
            }
        }

        // Botón de reintentar
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Prevenir memory leaks
    }
}