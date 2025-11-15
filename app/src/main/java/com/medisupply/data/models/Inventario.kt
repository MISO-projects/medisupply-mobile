package com.medisupply.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Inventario(
    @SerializedName("id")
    val id: String,

    @SerializedName("producto_id")
    val productoId: String,

    @SerializedName("lote")
    val lote: String,

    @SerializedName("fecha_vencimiento")
    val fechaVencimiento: String,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("ubicacion")
    val ubicacion: String,

    @SerializedName("temperatura_requerida")
    val temperaturaRequerida: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("condiciones_especiales")
    val condicionesEspeciales: String,

    @SerializedName("observaciones")
    val observaciones: String,

    @SerializedName("fecha_recepcion")
    val fechaRecepcion: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("producto_nombre")
    val productoNombre: String,

    @SerializedName("producto_sku")
    val productoSku: String,

    @SerializedName("producto_categoria")
    val categoria: String,

    @SerializedName("producto_imagen_url")
    val productoImagenUrl: String,

    @SerializedName("producto_unidad_medida")
    val productoUnidadMedida: String,

    @SerializedName("producto_tipo_almacenamiento")
    val productoTipoAlmacenamiento: String,

    @SerializedName("producto_precio_unitario")
    val productoPrecioUnitario: String,

    @SerializedName("producto_descripcion")
    val productoDescripcion: String,
) : Serializable

data class InventarioResponse(
    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("page_size")
    val pageSize: Int,

    @SerializedName("total_pages")
    val totalPages: Int,

    @SerializedName("items")
    val items: List<Inventario>
)

