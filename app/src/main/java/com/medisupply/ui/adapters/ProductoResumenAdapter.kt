package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R

class ProductoResumenAdapter : ListAdapter<ProductoConCantidad, ProductoResumenAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_resumen, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice: TextView = itemView.findViewById(R.id.product_price)
        private val productQuantity: TextView = itemView.findViewById(R.id.product_quantity)

        fun bind(item: ProductoConCantidad) {
            productName.text = item.producto.nombre
            productPrice.text = "$${item.producto.precioUnitario}"
            productQuantity.text = "${item.cantidad} unidades"
        }
    }

    private class ProductoDiffCallback : DiffUtil.ItemCallback<ProductoConCantidad>() {
        override fun areItemsTheSame(
            oldItem: ProductoConCantidad,
            newItem: ProductoConCantidad
        ): Boolean {
            return oldItem.producto.id == newItem.producto.id
        }

        override fun areContentsTheSame(
            oldItem: ProductoConCantidad,
            newItem: ProductoConCantidad
        ): Boolean {
            return oldItem == newItem
        }
    }
}
