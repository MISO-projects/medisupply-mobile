package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.medisupply.R
import com.medisupply.databinding.FragmentDetalleVisitaBinding // Importa el binding generado

class DetalleVisitaFragment : Fragment() {

    private var _binding: FragmentDetalleVisitaBinding? = null
    private val binding get() = _binding!!

    private var visitaId: String? = null

    companion object {
        private const val ARG_VISITA_ID = "visita_id"

        /**
         * Método 'Factory' para crear una nueva instancia de este fragment
         * pasando el ID de la visita como argumento.
         *
         * NOTA: Asumo que 'visita.id' es un String.
         * Si es un Int, cámbialo aquí y en los 'arguments'.
         */
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
            R.layout.fragment_detalle_visita, // Usamos el nuevo layout
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.detalleVisitaText.text = "seleccionaste la visita $visitaId"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}