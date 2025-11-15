package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.os.bundleOf 
import androidx.fragment.app.setFragmentResult 
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
                return@setOnClickListener
            }

            val motivoSeleccionado = requireView().findViewById<RadioButton>(selectedRadioId).text.toString()
            val detallesOpcionales = binding.editTextDetalles.text.toString().trim()
            val motivoFinal = if (detallesOpcionales.isNotEmpty()) {
                "$motivoSeleccionado. $detallesOpcionales"
            } else {
                motivoSeleccionado
            }
            setFragmentResult("request_cancelar_visita", bundleOf("motivo" to motivoFinal))

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NoRealizadaBottomSheet"
        fun newInstance() = NoRealizadaBottomSheet()
    }
}