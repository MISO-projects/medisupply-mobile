package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.data.models.PedidoResumenCliente
import com.medisupply.databinding.ItemPedidoClienteBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ClientPedidosAdapter(
    private val onItemClick: (PedidoResumenCliente) -> Unit
) : ListAdapter<PedidoResumenCliente, ClientPedidosAdapter.PedidoViewHolder>(PedidoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding = ItemPedidoClienteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PedidoViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PedidoViewHolder(
        private val binding: ItemPedidoClienteBinding,
        private val onItemClick: (PedidoResumenCliente) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: PedidoResumenCliente) {
            // Formatear la fecha con múltiples formatos posibles
            val fechaFormateada = formatearFecha(pedido.fechaCreacion)
            
            binding.fechaPedido.text = fechaFormateada
            binding.numeroPedido.text = "Pedido #${pedido.numeroPedido}"
            binding.estadoPedido.text = pedido.estado

            binding.root.setOnClickListener {
                onItemClick(pedido)
            }
        }
        
        private fun formatearFecha(fechaString: String): String {
            val outputFormat = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            
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
                }
            }
            
            return fechaString
        }
    }

    private class PedidoDiffCallback : DiffUtil.ItemCallback<PedidoResumenCliente>() {
        override fun areItemsTheSame(oldItem: PedidoResumenCliente, newItem: PedidoResumenCliente): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PedidoResumenCliente, newItem: PedidoResumenCliente): Boolean {
            return oldItem == newItem
        }
    }
}

