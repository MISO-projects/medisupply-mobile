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
import com.medisupply.data.models.Inventario
import com.medisupply.databinding.ItemProductoBinding

/**
 * Adaptador para la lista de productos
 */
class ProductosAdapter(
    private val onProductoClick: (Inventario) -> Unit = {}
) : ListAdapter<Inventario, ProductosAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding: ItemProductoBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_producto,
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(
        private val binding: ItemProductoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(inventario: Inventario) {
            binding.apply {
                // Configurar datos del producto
                nombreProducto.text = inventario.productoNombre
                stockProducto.text = root.context.getString(R.string.disponibles, inventario.cantidad)
                
                // Cambiar color del stock según disponibilidad
                val stockColor = if (inventario.cantidad > 50) {
                    root.context.getColor(R.color.md_theme_light_secondary)
                } else if (inventario.cantidad > 20) {
                    root.context.getColor(R.color.text_secondary)
                } else {
                    root.context.getColor(android.R.color.holo_orange_dark)
                }
                stockProducto.setTextColor(stockColor)

                // Cargar imagen con Glide
                Glide.with(imagenProducto.context)
                    .load(inventario.productoImagenUrl)
                    .placeholder(R.drawable.ic_hospital_placeholder)
                    .error(R.drawable.ic_hospital_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imagenProducto)

                // Configurar click listener
                root.setOnClickListener {
                    onProductoClick(inventario)
                }

                executePendingBindings()
            }
        }
    }

    /**
     * DiffUtil callback para optimizar las actualizaciones de la lista
     */
    private class ProductoDiffCallback : DiffUtil.ItemCallback<Inventario>() {
        override fun areItemsTheSame(oldItem: Inventario, newItem: Inventario): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Inventario, newItem: Inventario): Boolean {
            return oldItem == newItem
        }
    }
}

