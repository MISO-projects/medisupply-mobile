package com.medisupply.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R
import com.medisupply.ui.fragments.ProductoDetalle

class ProductoDetalleAdapter :
        ListAdapter<ProductoDetalle, ProductoDetalleAdapter.ProductoViewHolder>(
                ProductoDetalleDiffCallback()
        ) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
    val view =
            LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_producto_detalle, parent, false)
    return ProductoViewHolder(view)
  }

  override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val productImage: ImageView = itemView.findViewById(R.id.product_image)
    private val productName: TextView = itemView.findViewById(R.id.product_name)
    private val productQuantity: TextView = itemView.findViewById(R.id.product_quantity)

    fun bind(item: ProductoDetalle) {
      productName.text = item.nombre
      productQuantity.text = itemView.context.getString(R.string.cantidad, item.cantidad)

      // TODO: Load actual product image using Glide or similar
      // For now, the placeholder background will be shown
    }
  }

  private class ProductoDetalleDiffCallback : DiffUtil.ItemCallback<ProductoDetalle>() {
    override fun areItemsTheSame(oldItem: ProductoDetalle, newItem: ProductoDetalle): Boolean {
      return oldItem.nombre == newItem.nombre
    }

    override fun areContentsTheSame(oldItem: ProductoDetalle, newItem: ProductoDetalle): Boolean {
      return oldItem == newItem
    }
  }
}
