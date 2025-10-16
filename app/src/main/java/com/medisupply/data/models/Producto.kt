package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Producto
 */
data class Producto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("categoria")
    val categoria: String,
    
    @SerializedName("imagen_url")
    val imagenUrl: String,
    
    @SerializedName("stock_disponible")
    val stockDisponible: Int,
    
    @SerializedName("disponible")
    val disponible: Boolean,
    
    @SerializedName("precio_unitario")
    val precioUnitario: String,
    
    @SerializedName("unidad_medida")
    val unidadMedida: String,
    
    @SerializedName("descripcion")
    val descripcion: String
)

/**
 * Respuesta del endpoint de productos
 */
data class ProductoResponse(
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("productos")
    val productos: List<Producto>
)

