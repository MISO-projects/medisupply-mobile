package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R
import com.medisupply.data.models.Producto

class ProductoSearchAdapter(
    private val onProductoClick: (Producto) -> Unit
) : ListAdapter<Producto, ProductoSearchAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_search, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.producto_nombre)
        private val categoriaTextView: TextView = itemView.findViewById(R.id.producto_categoria)
        private val precioTextView: TextView = itemView.findViewById(R.id.producto_precio)
        private val stockTextView: TextView = itemView.findViewById(R.id.producto_stock)

        fun bind(producto: Producto) {
            nombreTextView.text = producto.nombre
            categoriaTextView.text = producto.categoria
            precioTextView.text = "$${producto.precioUnitario}"
            stockTextView.text = "Stock: ${producto.stockDisponible}"

            itemView.setOnClickListener {
                onProductoClick(producto)
            }
        }
    }

    private class ProductoDiffCallback : DiffUtil.ItemCallback<Producto>() {
        override fun areItemsTheSame(oldItem: Producto, newItem: Producto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Producto, newItem: Producto): Boolean {
            return oldItem == newItem
        }
    }
}
