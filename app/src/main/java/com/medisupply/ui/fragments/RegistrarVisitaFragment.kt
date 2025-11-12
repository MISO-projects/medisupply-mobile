package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.medisupply.R
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentRegistrarVisitaBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.medisupply.ui.viewmodels.RegistrarVisitaViewModel
import com.medisupply.ui.viewmodels.RegistrarVisitaViewModelFactory
import java.util.Calendar
import java.util.Locale

class RegistrarVisitaFragment : Fragment() {

    private var _binding: FragmentRegistrarVisitaBinding? = null
    private val binding get() = _binding!!

    private var visitaId: String? = null

    private val viewModel: RegistrarVisitaViewModel by viewModels {
        if (visitaId == null) {
            throw IllegalStateException("El ID de la visita no puede ser nulo al crear el ViewModel")
        }
        val repository = VisitasRepository(NetworkServiceAdapter.getApiService())
        RegistrarVisitaViewModelFactory(repository, visitaId!!)
    }

    private var horaInicioSeleccionada: Pair<Int, Int>? = null
    private var horaFinSeleccionada: Pair<Int, Int>? = null

    companion object {
        private const val ARG_VISITA_ID = "visita_id"

        fun newInstance(id: String): RegistrarVisitaFragment {
            return RegistrarVisitaFragment().apply {
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

        if (visitaId == null) {
            Toast.makeText(requireContext(), "Error: ID de visita no encontrado", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registrar_visita,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Mensaje de prueba 
        // Toast.makeText(requireContext(), "ID de visita a registrar: $visitaId", Toast.LENGTH_LONG).show()

        configurarListeners()
        observarViewModel()
    }

    /**
     * Configura todos los listeners de la vista
     */
    private fun configurarListeners() {
        // Listeners de Hora
        binding.startTimeEditText.setOnClickListener {
            mostrarSelectorDeHora(esHoraInicio = true)
        }
        binding.endTimeEditText.setOnClickListener {
            mostrarSelectorDeHora(esHoraInicio = false)
        }

        // Listener de Cancelar
        binding.btnCancelar.setOnClickListener {
            // Solo permite cancelar si no está cargando
            if (viewModel.isLoading.value != true) {
                parentFragmentManager.popBackStack()
            }
        }

        // Listener del botón Guardar
        binding.btnGuardar.setOnClickListener {
            intentarGuardarVisita()
        }
    }

    /**
     * Observa los LiveData del ViewModel
     */
    private fun observarViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Muestra/oculta el ProgressBar
            binding.loadingProgressBar.isVisible = isLoading
            // Deshabilita los botones mientras carga
            binding.btnGuardar.isEnabled = !isLoading
            binding.btnCancelar.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.registroExitoso.observe(viewLifecycleOwner) { visitaActualizada ->
            if (visitaActualizada != null) {
                Toast.makeText(requireContext(), "Visita guardada con éxito", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    /**
     * Valida los campos y llama al ViewModel para guardar
     */
    private fun intentarGuardarVisita() {
        val horaInicio = binding.startTimeEditText.text.toString()
        val horaFin = binding.endTimeEditText.text.toString()
        val contacto = binding.contactEditText.text.toString()
        val detalle = binding.descriptionEditText.text.toString()

        if (horaInicio.isBlank() || horaFin.isBlank() || contacto.isBlank() || detalle.isBlank()) {
            Toast.makeText(requireContext(), "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.guardarVisita(detalle, contacto, horaInicio, horaFin)
    }


    private fun mostrarSelectorDeHora(esHoraInicio: Boolean) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val (defaultHour, defaultMinute) = if (esHoraInicio) {
            horaInicioSeleccionada ?: (currentHour to currentMinute)
        } else {
            horaFinSeleccionada ?: (currentHour to currentMinute)
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(defaultHour) 
            .setMinute(defaultMinute) 
            .setTitleText(
                if (esHoraInicio) "Seleccionar hora de inicio"
                else "Seleccionar hora de fin"
            )
            .build()

        picker.addOnPositiveButtonClickListener {
            val selectedHour = picker.hour
            val selectedMinute = picker.minute
            val horaFormateada = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                selectedHour,
                selectedMinute
            )

            if (esHoraInicio) {
                horaInicioSeleccionada = selectedHour to selectedMinute
                binding.startTimeEditText.setText(horaFormateada)

                if (!esHoraFinValida()) {
                    horaFinSeleccionada = null
                    binding.endTimeEditText.setText("")
                    Toast.makeText(requireContext(), "Hora de fin reiniciada", Toast.LENGTH_SHORT).show()
                }

            } else {
                if (esHoraFinValida(selectedHour, selectedMinute)) {
                    horaFinSeleccionada = selectedHour to selectedMinute
                    binding.endTimeEditText.setText(horaFormateada)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "La hora de fin no puede ser anterior a la de inicio",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        picker.show(parentFragmentManager, "TIME_PICKER_TAG")
    }

    private fun esHoraFinValida(finHour: Int, finMinute: Int): Boolean {
        val (startHour, startMinute) = horaInicioSeleccionada ?: return true
        val inicioTotalMinutos = startHour * 60 + startMinute
        val finTotalMinutos = finHour * 60 + finMinute
        return finTotalMinutos >= inicioTotalMinutos
    }

    private fun esHoraFinValida(): Boolean {
        val (finHour, finMinute) = horaFinSeleccionada ?: return true
        return esHoraFinValida(finHour, finMinute)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}