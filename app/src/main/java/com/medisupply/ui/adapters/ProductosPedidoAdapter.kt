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
import java.io.Serializable

/**
 * Wrapper para producto con cantidad seleccionada
 */
data class ProductoConCantidad(
    val producto: Producto,
    var cantidad: Int = 0
) : Serializable

class ProductosPedidoAdapter(
    private val onCantidadChanged: (ProductoConCantidad) -> Unit
) : ListAdapter<ProductoConCantidad, ProductosPedidoAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_pedido, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productAvailability: TextView = itemView.findViewById(R.id.product_availability)
        private val productQuantity: TextView = itemView.findViewById(R.id.product_quantity)
        private val btnDecrease: TextView = itemView.findViewById(R.id.btn_decrease)
        private val btnIncrease: TextView = itemView.findViewById(R.id.btn_increase)

        fun bind(item: ProductoConCantidad) {
            productName.text = item.producto.nombre
            productAvailability.text = "${item.producto.stockDisponible} unidades disponibles"
            productQuantity.text = item.cantidad.toString()

            btnDecrease.isEnabled = item.cantidad > 0
            btnIncrease.isEnabled = item.cantidad < item.producto.stockDisponible

            btnDecrease.setOnClickListener {
                if (item.cantidad > 0) {
                    item.cantidad--
                    productQuantity.text = item.cantidad.toString()
                    btnDecrease.isEnabled = item.cantidad > 0
                    btnIncrease.isEnabled = true
                    onCantidadChanged(item)
                }
            }

            btnIncrease.setOnClickListener {
                if (item.cantidad < item.producto.stockDisponible) {
                    item.cantidad++
                    productQuantity.text = item.cantidad.toString()
                    btnDecrease.isEnabled = true
                    btnIncrease.isEnabled = item.cantidad < item.producto.stockDisponible
                    onCantidadChanged(item)
                }
            }
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
            return oldItem.producto == newItem.producto && oldItem.cantidad == newItem.cantidad
        }
    }
}
