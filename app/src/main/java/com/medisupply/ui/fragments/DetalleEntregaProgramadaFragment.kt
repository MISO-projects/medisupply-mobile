package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.medisupply.R
import com.medisupply.data.models.EntregaProgramadaItem
import com.medisupply.databinding.FragmentDetalleEntregaProgramadaBinding
import java.text.SimpleDateFormat
import java.util.Locale

class DetalleEntregaProgramadaFragment : Fragment() {

    private var _binding: FragmentDetalleEntregaProgramadaBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ENTREGA = "entrega_programada"

        fun newInstance(entrega: EntregaProgramadaItem): DetalleEntregaProgramadaFragment {
            return DetalleEntregaProgramadaFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_ENTREGA, entrega)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detalle_entrega_programada,
            container,
            false
        )

        loadEntregaData()

        return binding.root
    }

    private fun loadEntregaData() {
        val entrega = arguments?.getParcelable<EntregaProgramadaItem>(ARG_ENTREGA)
        entrega?.let {
            // Información del pedido
            binding.numeroOrden.text = it.pedido.numeroOrden
            binding.estadoPedido.text = it.pedido.estado
            binding.valorTotal.text = "S/ ${String.format("%.2f", it.pedido.valorTotal)}"
            binding.cantidadItems.text = "${it.pedido.cantidadItems} ${if (it.pedido.cantidadItems == 1) "item" else "items"}"
            binding.nombreCliente.text = it.pedido.nombreCliente

            // Información de la ruta
            binding.fechaRuta.text = formatearFecha(it.ruta.fecha)
            binding.estadoRuta.text = formatearEstado(it.ruta.estado)
            binding.bodegaOrigen.text = it.ruta.bodegaOrigen
            binding.conductorNombre.text = it.ruta.conductorNombre ?: "Sin asignar"
            binding.vehiculoPlaca.text = it.ruta.vehiculoPlaca ?: "N/A"
            binding.vehiculoInfo.text = it.ruta.vehiculoInfo ?: "N/A"
            binding.condicionesAlmacenamiento.text = it.ruta.condicionesAlmacenamiento ?: "N/A"

            // Información de la parada
            binding.direccionEntrega.text = it.parada.direccion
            binding.contacto.text = it.parada.contacto
            binding.estadoParada.text = formatearEstado(it.parada.estado)
            binding.ordenParada.text = "#${it.parada.orden}"
        }
    }

    private fun formatearFecha(fechaString: String): String {
        val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
        
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        )
        
        for (inputFormat in inputFormats) {
            try {
                inputFormat.isLenient = false
                val date = inputFormat.parse(fechaString)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                // Continuar con el siguiente formato
            }
        }
        
        return fechaString
    }

    private fun formatearEstado(estado: String): String {
        return when {
            estado.contains("_") -> estado.replace("_", " ")
            else -> estado
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

