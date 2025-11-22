package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R
import com.medisupply.data.models.EntregaProgramadaItem
import com.medisupply.databinding.ItemEntregaProgramadaBinding
import java.text.SimpleDateFormat
import java.util.Locale

class EntregasProgramadasAdapter(
    private val onItemClick: (EntregaProgramadaItem) -> Unit
) : ListAdapter<EntregaProgramadaItem, EntregasProgramadasAdapter.EntregaViewHolder>(
    EntregaDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntregaViewHolder {
        val binding = ItemEntregaProgramadaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EntregaViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: EntregaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EntregaViewHolder(
        private val binding: ItemEntregaProgramadaBinding,
        private val onItemClick: (EntregaProgramadaItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entrega: EntregaProgramadaItem) {
            // Formatear la fecha según el mockup: "26 de sept, 2025"
            val fechaFormateada = formatearFecha(entrega.ruta.fecha)
            binding.fechaEntrega.text = fechaFormateada

            // Número de orden del pedido
            binding.numeroOrden.text = entrega.pedido.numeroOrden

            // Estado de la ruta
            binding.estadoEntrega.text = formatearEstado(entrega.ruta.estado)

            // Nombre del conductor (puede ser null)
            val conductorNombre = entrega.ruta.conductorNombre ?: binding.root.context.getString(R.string.sin_asignar)
            binding.conductorEntrega.text = conductorNombre

            binding.root.setOnClickListener {
                onItemClick(entrega)
            }
        }

        private fun formatearFecha(fechaString: String): String {
            val outputFormat = SimpleDateFormat("d 'de' MMM, yyyy", Locale("es", "ES"))
            
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
            // Formatear el estado para mostrarlo de manera más legible
            // El estado puede venir como "Pendiente", "En Curso", "Completada", etc.
            return when {
                estado.contains("_") -> estado.replace("_", " ")
                else -> estado
            }
        }
    }

    private class EntregaDiffCallback : DiffUtil.ItemCallback<EntregaProgramadaItem>() {
        override fun areItemsTheSame(
            oldItem: EntregaProgramadaItem,
            newItem: EntregaProgramadaItem
        ): Boolean {
            return oldItem.parada.id == newItem.parada.id
        }

        override fun areContentsTheSame(
            oldItem: EntregaProgramadaItem,
            newItem: EntregaProgramadaItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}

