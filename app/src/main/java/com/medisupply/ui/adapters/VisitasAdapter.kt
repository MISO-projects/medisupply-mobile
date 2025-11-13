package com.medisupply.ui.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.databinding.ItemVisitaBinding

class VisitasAdapter(
    private val onItemClicked: (RutaVisitaItem) -> Unit
) : ListAdapter<RutaVisitaItem, VisitasAdapter.VisitaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitaViewHolder {
        val binding = ItemVisitaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VisitaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitaViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClicked)
    }

    class VisitaViewHolder(private val binding: ItemVisitaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(visita: RutaVisitaItem, onItemClicked: (RutaVisitaItem) -> Unit) {
            binding.textNombreCliente.text = visita.nombre
            val dirLimp = limpiarDireccion(visita.direccion)
            binding.textDireccionCliente.text = dirLimp
            binding.textHoraVisita.text = visita.horaDeLaCita

            // --- Lógica para el ESTADO ---
            val context = binding.root.context
            binding.textEstado.isVisible = true
            binding.textNombreCliente.paintFlags = 0 // Resetear strike-through

            when (visita.estado) {
                "PENDIENTE" -> {
                    binding.textEstado.text = "PENDIENTE"
                    binding.textEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pendiente)
                }
                "REALIZADA" -> {
                    binding.textEstado.text = "REALIZADA"
                    binding.textEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_realizada)
                }
                "CANCELADA" -> {
                    binding.textEstado.text = "CANCELADA"
                    binding.textEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_cancelada)
                    // Tachar el nombre si está cancelada
                    binding.textNombreCliente.paintFlags = binding.textNombreCliente.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
                else -> {
                    binding.textEstado.isVisible = false
                }
            }
            
            binding.root.setOnClickListener {
                onItemClicked(visita)
            }
        }

        private fun limpiarDireccion(direccionCompleta: String?): String {
            if (direccionCompleta.isNullOrEmpty()) return "N/A"
            val partes = direccionCompleta.split(",")
            if (partes.size > 2) {
                return partes.subList(2, partes.size).joinToString(",")
            }
            return direccionCompleta
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<RutaVisitaItem>() {
        override fun areItemsTheSame(oldItem: RutaVisitaItem, newItem: RutaVisitaItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: RutaVisitaItem, newItem: RutaVisitaItem): Boolean {
            return oldItem == newItem
        }
    }
}