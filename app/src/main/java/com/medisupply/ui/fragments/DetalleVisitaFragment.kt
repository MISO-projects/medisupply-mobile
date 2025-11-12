package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.medisupply.R
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentDetalleVisitaBinding
import com.medisupply.databinding.ItemDetalleFilaBinding
import com.medisupply.ui.viewmodels.DetalleVisitaViewModel
import com.medisupply.ui.viewmodels.DetalleVisitaViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DetalleVisitaFragment : Fragment() {

    private var _binding: FragmentDetalleVisitaBinding? = null
    private val binding get() = _binding!!

    private var visitaId: String? = null

    private val viewModel: DetalleVisitaViewModel by viewModels {
        val repository = VisitasRepository(NetworkServiceAdapter.getApiService())
        DetalleVisitaViewModelFactory(repository, visitaId!!)
    }

    
    // Parsea: "2025-11-11T13:45:00Z"
    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    // Formatea: "10:00 AM"
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())


    companion object {
        private const val ARG_VISITA_ID = "visita_id"

        fun newInstance(id: String): DetalleVisitaFragment {
            return DetalleVisitaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VISITA_ID, id)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            visitaId = it.getString(ARG_VISITA_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detalle_visita,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (visitaId == null) {
            Toast.makeText(requireContext(), "Error: ID de visita no encontrado", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack() // Regresar
            return
        }
        setupToolbar()

        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.toolbarDetalle.setNavigationIcon(
            androidx.appcompat.R.drawable.abc_ic_ab_back_material
        )
        binding.toolbarDetalle.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.visita.observe(viewLifecycleOwner) { visita ->
            visita?.let { poblarDatos(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            if (isLoading) {
                binding.errorView.isVisible = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.errorView.isVisible = error != null
            if (error != null) {
                binding.errorText.text = error
            }
        }
    }

    private fun setupListeners() {
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }

        binding.btnIniciarVisita.setOnClickListener {
            if (visitaId == null) {
                Toast.makeText(requireContext(), "Error: ID de visita no disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val registrarVisitaFragment = RegistrarVisitaFragment.newInstance(visitaId!!)

            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, registrarVisitaFragment)
                .addToBackStack(null) // Para poder volver atrás
                .commit()
        }

        binding.btnMarcarNoRealizada.setOnClickListener {
            // Crea una instancia y muestra el BottomSheet
            val bottomSheet = NoRealizadaBottomSheet.newInstance()
            bottomSheet.show(parentFragmentManager, NoRealizadaBottomSheet.TAG)
        }
    }



    private fun poblarDatos(visita: VisitaDetalle) {
        binding.textNombreInstitucionHeader.text = visita.nombreInstitucion
        binding.textHoraVisitaHeader.text = formatHora(visita.fechaVisitaProgramada)
        setupFila(binding.rowNombre, "Nombre", visita.nombreInstitucion)
        setupFila(binding.rowDireccion, "Dirección", visita.direccion)
        setupFila(binding.rowContacto, "Contacto", visita.clienteContacto ?: "N/A")
        setupFila(binding.rowProductos, "Productos Preferidos", "N/A")
        setupFila(binding.rowTiempo, "Tiempo de Desplazamiento", "N/A")
        setupFila(binding.rowNotas, "Notas de Visita anterior", visita.detalle ?: "N/A")
    }

    /**
     * Helper para configurar una fila del layout incluido
     */
    private fun setupFila(filaBinding: ItemDetalleFilaBinding, label: String, value: String) {
        filaBinding.itemLabel.text = label
        filaBinding.itemValue.text = value
    }

    private fun formatHora(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return "N/A"
        return try {
            val date = isoFormatter.parse(isoDate)
            if (date != null) {
                timeFormatter.format(date)
            } else {
                "N/A"
                 }
        } catch (e: Exception) {
            "N/A"
        }
    }
}