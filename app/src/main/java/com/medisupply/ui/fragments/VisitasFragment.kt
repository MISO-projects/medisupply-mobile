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
        setupDateSelector() // <-- Cambiamos el nombre de la función
        observeViewModel()

        return binding.root
    }

    private fun setupViewModel() {
        val apiService = NetworkServiceAdapter.getApiService()
        val repository = VisitasRepository(apiService)
        val factory = VisitasViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[VisitasViewModel::class.java]
    }

    private fun setupRecyclerView() {
        visitasAdapter = VisitasAdapter { visita ->
            // TODO: Manejar clic en la visita
        }

        binding.visitasRecyclerView.apply {
            adapter = visitasAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    // --- FUNCIÓN MODIFICADA ---
    private fun setupDateSelector() {
        // 1. Clic en la barra para MOSTRAR/OCULTAR el calendario
        binding.dateSelectorBar.setOnClickListener {
            toggleCalendarVisibility()
        }

        // 2. Al seleccionar una fecha en el calendario...
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            viewModel.seleccionarFecha(calendar) // Cargar datos
            binding.calendarView.isVisible = false // Ocultar calendario
        }
    }

    // --- NUEVA FUNCIÓN HELPER ---
    private fun toggleCalendarVisibility() {
        // Simplemente invierte la visibilidad actual
        binding.calendarView.isVisible = !binding.calendarView.isVisible
    }
    // --------------------------

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

        // --- NUEVO OBSERVADOR ---
        // 3. Observar la fecha formateada y actualizar la UI
        viewModel.fechaFormateada.observe(viewLifecycleOwner) { fechaFormateada ->
            binding.textFechaSeleccionada.text = fechaFormateada
        }
        // -------------------------

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