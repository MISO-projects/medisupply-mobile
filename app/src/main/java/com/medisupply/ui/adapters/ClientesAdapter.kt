package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.medisupply.R
import com.medisupply.data.models.Cliente
import com.medisupply.databinding.ItemClienteBinding

/**
 * Adaptador para la lista de clientes
 */
class ClientesAdapter(
    private val onClienteClick: (Cliente) -> Unit = {}
) : ListAdapter<Cliente, ClientesAdapter.ClienteViewHolder>(ClienteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val binding: ItemClienteBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_cliente,
            parent,
            false
        )
        return ClienteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ClienteViewHolder(
        private val binding: ItemClienteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cliente: Cliente) {
            binding.apply {
                // Configurar datos del cliente
                nombreCliente.text = cliente.nombre
                nitCliente.text = root.context.getString(R.string.nit_prefix, cliente.nit)

                // Cargar logo con Glide - usar URL fija para todos por ahora
                Glide.with(logoCliente.context)
                    .load("https://upload.wikimedia.org/wikipedia/commons/1/12/Logo_nueva_eps.png")
                    .placeholder(R.drawable.ic_hospital_placeholder)
                    .error(R.drawable.ic_hospital_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(logoCliente)

                // Configurar click listener
                root.setOnClickListener {
                    onClienteClick(cliente)
                }

                executePendingBindings()
            }
        }
    }

    /**
     * DiffUtil callback para optimizar las actualizaciones de la lista
     */
    private class ClienteDiffCallback : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem == newItem
        }
    }
}
