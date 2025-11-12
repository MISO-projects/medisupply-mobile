package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton // <-- Importa RadioButton
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.medisupply.databinding.FragmentNoRealizadaBottomBinding

class NoRealizadaBottomSheet : BottomSheetDialogFragment() {
    private var _binding: FragmentNoRealizadaBottomBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoRealizadaBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCancelarBottomSheet.setOnClickListener {
            dismiss()
        }

        binding.btnConfirmarBottomSheet.setOnClickListener {

            val selectedRadioId = binding.radioGroupMotivos.checkedRadioButtonId
            if (selectedRadioId == -1) {
                Toast.makeText(requireContext(), "Por favor, selecciona un motivo", Toast.LENGTH_SHORT).show()
            } else {
                // Usamos requireView() (o binding.root) para encontrar el RadioButton por su ID
                val motivoSeleccionado = requireView().findViewById<RadioButton>(selectedRadioId).text.toString()

                val detallesOpcionales = binding.editTextDetalles.text.toString()

                val mensajeBase = "Marcaste como no realizada porque $motivoSeleccionado"

                val mensajeFinal = if (detallesOpcionales.isNotBlank()) {
                    "$mensajeBase: $detallesOpcionales" // Con detalles
                } else {
                    mensajeBase // Sin detalles
                }

                Toast.makeText(requireContext(), mensajeFinal, Toast.LENGTH_LONG).show()

                dismiss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NoRealizadaBottomSheet"

        fun newInstance(): NoRealizadaBottomSheet {
            return NoRealizadaBottomSheet()
        }
    }
}