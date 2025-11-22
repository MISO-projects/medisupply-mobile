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
        val itemActual = getItem(position)
        val context = holder.binding.root.context

        val textoTiempoViaje: String

        // Verificamos si la visita está PENDIENTE
        if (itemActual.estado == "PENDIENTE") {
            // Verificamos si es el primer ítem PENDIENTE en la lista
            // (El backend ya nos da las PENDIENTES primero, así que position == 0 es válido)
            if (position == 0) {
                // Formato: "a 14 mins de tu ubicación"
                textoTiempoViaje = context.getString(
                    R.string.formato_tiempo_viaje_primero,
                    itemActual.horaDeLaCita // "14 mins"
                )
            } else {
                // Es una visita PENDIENTE intermedia
                val itemAnterior = getItem(position - 1)

                // Formato: "a 7 mins de institucion3"
                textoTiempoViaje = context.getString(
                    R.string.formato_tiempo_viaje_siguiente,
                    itemActual.horaDeLaCita, // "7 mins"
                    itemAnterior.nombre      // "institucion3"
                )
            }
        } else {
            // Es REALIZADA o CANCELADA, el backend ya nos envía "N/A"
            textoTiempoViaje = itemActual.horaDeLaCita // "N/A"
        }

        // Llamamos a bind con el texto ya formateado
        holder.bind(itemActual, textoTiempoViaje, onItemClicked)
    }

    class VisitaViewHolder(val binding: ItemVisitaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            visita: RutaVisitaItem,
            textoTiempoViaje: String,
            onItemClicked: (RutaVisitaItem) -> Unit
        ) {
            binding.textNombreCliente.text = visita.nombre
            val dirLimp = limpiarDireccion(visita.direccion)
            binding.textDireccionCliente.text = dirLimp
            binding.textHoraVisita.text = textoTiempoViaje

            val context = binding.root.context
            binding.textEstado.isVisible = true
            binding.textNombreCliente.paintFlags = 0 // Resetear strike-through

            when (visita.estado) {
                "PENDIENTE" -> {
                    binding.textEstado.text = context.getString(R.string.pendiente)
                    binding.textEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_pendiente)
                }
                "REALIZADA", "TOMADA" -> {
                    binding.textEstado.text = when (visita.estado) {
                        "REALIZADA" -> context.getString(R.string.realizada)
                        "TOMADA" -> context.getString(R.string.tomada)
                        else -> visita.estado
                    }
                    binding.textEstado.background = ContextCompat.getDrawable(context, R.drawable.bg_badge_realizada)
                }
                "CANCELADA" -> {
                    binding.textEstado.text = context.getString(R.string.cancelada)
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
            if (direccionCompleta.isNullOrEmpty()) return binding.root.context.getString(R.string.na)
            val partes = direccionCompleta.split(",")
            // Formato: "Lat,Lon,Direccion"
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