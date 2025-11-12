package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.medisupply.R
import com.medisupply.databinding.FragmentRegistrarVisitaBinding
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.util.Calendar // <-- NUEVO: Para obtener la hora actual
import java.util.Locale

class RegistrarVisitaFragment : Fragment() {

    private var _binding: FragmentRegistrarVisitaBinding? = null
    private val binding get() = _binding!!

    private var visitaId: String? = null

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

        Toast.makeText(
            requireContext(),
            "ID de visita a registrar: $visitaId",
            Toast.LENGTH_LONG
        ).show()

        // Configurar Listeners
        binding.startTimeEditText.setOnClickListener {
            // "true" indica que es para la hora de inicio
            mostrarSelectorDeHora(esHoraInicio = true)
        }

        binding.endTimeEditText.setOnClickListener {
            mostrarSelectorDeHora(esHoraInicio = false)
        }

        binding.btnCancelar.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun mostrarSelectorDeHora(esHoraInicio: Boolean) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Si ya había una hora seleccionada, la usamos; si no, usamos la actual.
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
                    binding.endTimeEditText.setText("") // Limpiar si no es válida
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

    /**
     * Comprueba si una hora de fin (nueva) es válida
     * comparada con la hora de inicio ya guardada.
     */
    private fun esHoraFinValida(finHour: Int, finMinute: Int): Boolean {
        val (startHour, startMinute) = horaInicioSeleccionada ?: return true
        val inicioTotalMinutos = startHour * 60 + startMinute
        val finTotalMinutos = finHour * 60 + finMinute

        return finTotalMinutos >= inicioTotalMinutos // La hora fin debe ser mayor o igual
    }

    /**
     * Comprueba si la hora de fin *existente* (la guardada en [horaFinSeleccionada])
     * sigue siendo válida.
     */
    private fun esHoraFinValida(): Boolean {
        // Si no hay hora de fin guardada, no hay nada que re-validar
        val (finHour, finMinute) = horaFinSeleccionada ?: return true
        return esHoraFinValida(finHour, finMinute)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}