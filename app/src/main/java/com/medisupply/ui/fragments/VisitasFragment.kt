package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentVisitasBinding
import com.medisupply.ui.adapters.VisitasAdapter
import com.medisupply.ui.viewmodels.VisitasViewModel
import com.medisupply.ui.viewmodels.VisitasViewModelFactory
import com.medisupply.data.session.SessionManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VisitasFragment : Fragment() {

    private var _binding: FragmentVisitasBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: VisitasViewModel
    private lateinit var visitasAdapter: VisitasAdapter

    private val uiDateFormatter = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault())

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

    // --- onViewCreated para configurar los listeners de la vista ---
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar el listener del calendario
        binding.calendarView.setOnDateChangeListener { calendarView, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)

            // Notificar al ViewModel la nueva fecha seleccionada
            viewModel.seleccionarFecha(calendar.time)
        }

        // Configurar el botón de reintentar
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }


    private fun setupViewModel() {
        val application = requireActivity().application
        val apiService = NetworkServiceAdapter.getApiService()
        val repository = VisitasRepository(apiService)
        val sessionManager = SessionManager(application.applicationContext)

        val factory = VisitasViewModelFactory(repository, sessionManager)
        viewModel = ViewModelProvider(this, factory)[VisitasViewModel::class.java]
    }

    private fun setupRecyclerView() {
        visitasAdapter = VisitasAdapter { visita ->
            onVisitaClick(visita)
            Toast.makeText(
                requireContext(),
                "Visita seleccionada: ${visita.id}",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.visitasRecyclerView.apply {
            adapter = visitasAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun onVisitaClick(visita: RutaVisitaItem) {
        val detailFragment = DetalleVisitaFragment.newInstance(visita.id)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null) // Esto permite al usuario presionar "atrás"
            .commit()
    }

    private fun observeViewModel() {
        viewModel.rutas.observe(viewLifecycleOwner) { rutas ->
            visitasAdapter.submitList(rutas)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            if (!isLoading) {
                val currentError = viewModel.error.value
                val currentRutas = viewModel.rutas.value

                binding.errorView.isVisible = currentError != null
                binding.visitasRecyclerView.isVisible = currentError == null && !currentRutas.isNullOrEmpty()
                binding.emptyView.isVisible = currentError == null && currentRutas.isNullOrEmpty()
            } else {
                binding.visitasRecyclerView.isVisible = false
                binding.errorView.isVisible = false
                binding.emptyView.isVisible = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            val isError = error != null
            binding.errorView.isVisible = isError
            if (isError) {
                binding.errorText.text = error
                binding.visitasRecyclerView.isVisible = false
                binding.emptyView.isVisible = false
            }
        }

        // --- Observador para la fecha seleccionada ---
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            binding.selectedDateTitle.text = "Visitas para ${uiDateFormatter.format(date)}"
            if (binding.calendarView.date != date.time) {
                binding.calendarView.date = date.time
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}