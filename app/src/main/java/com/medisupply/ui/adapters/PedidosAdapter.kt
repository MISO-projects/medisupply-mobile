package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R
import com.medisupply.data.models.Pedido
import com.medisupply.databinding.ItemPedidoBinding

class PedidosAdapter(
    private val onPedidoClick: (Pedido) -> Unit = {}
) : ListAdapter<Pedido, PedidosAdapter.PedidoViewHolder>(PedidoDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val binding: ItemPedidoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_pedido,
            parent,
            false
        )
        return PedidoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PedidoViewHolder(
        private val binding: ItemPedidoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pedido: Pedido) {
            binding.apply {
                // Configurar datos del pedido
                numeroPedido.text = pedido.numeroPedido
                nombreCliente.text = pedido.nombreCliente
                estadoPedido.text = pedido.estado

                // Configurar click listener
                root.setOnClickListener {
                    onPedidoClick(pedido)
                }

                executePendingBindings()
            }
        }
    }


    private class PedidoDiffCallback : DiffUtil.ItemCallback<Pedido>() {
        override fun areItemsTheSame(oldItem: Pedido, newItem: Pedido): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pedido, newItem: Pedido): Boolean {
            return oldItem == newItem
        }
    }
}