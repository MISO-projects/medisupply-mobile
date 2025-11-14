package com.medisupply.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Modelo de datos para una Parada de entrega
 */
@Parcelize
data class Parada(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("pedido_id")
    val pedidoId: String,
    
    @SerializedName("direccion")
    val direccion: String,
    
    @SerializedName("contacto")
    val contacto: String,
    
    @SerializedName("latitud")
    val latitud: Double,
    
    @SerializedName("longitud")
    val longitud: Double,
    
    @SerializedName("orden")
    val orden: Int,
    
    @SerializedName("estado")
    val estado: String, // Pendiente | En_Camino | Entregada
    
    @SerializedName("fecha_creacion")
    val fechaCreacion: String,
    
    @SerializedName("fecha_actualizacion")
    val fechaActualizacion: String
) : Parcelable

/**
 * Modelo de datos para información del Pedido en una entrega programada
 */
@Parcelize
data class PedidoEntrega(
    @SerializedName("numero_orden")
    val numeroOrden: String,
    
    @SerializedName("estado")
    val estado: String,
    
    @SerializedName("valor_total")
    val valorTotal: Double,
    
    @SerializedName("cantidad_items")
    val cantidadItems: Int,
    
    @SerializedName("nombre_cliente")
    val nombreCliente: String
) : Parcelable

/**
 * Modelo de datos para información de la Ruta
 */
@Parcelize
data class RutaEntrega(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("fecha")
    val fecha: String,
    
    @SerializedName("bodega_origen")
    val bodegaOrigen: String,
    
    @SerializedName("estado")
    val estado: String, // Pendiente | En Curso | Completada
    
    @SerializedName("vehiculo_placa")
    val vehiculoPlaca: String?,
    
    @SerializedName("vehiculo_info")
    val vehiculoInfo: String?,
    
    @SerializedName("conductor_nombre")
    val conductorNombre: String?,
    
    @SerializedName("condiciones_almacenamiento")
    val condicionesAlmacenamiento: String?
) : Parcelable

/**
 * Modelo de datos para un item de entrega programada
 */
@Parcelize
data class EntregaProgramadaItem(
    @SerializedName("parada")
    val parada: Parada,
    
    @SerializedName("pedido")
    val pedido: PedidoEntrega,
    
    @SerializedName("ruta")
    val ruta: RutaEntrega
) : Parcelable

/**
 * Respuesta del endpoint de entregas programadas
 */
data class EntregasProgramadasResponse(
    @SerializedName("data")
    val data: List<EntregaProgramadaItem>,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("page_size")
    val pageSize: Int,
    
    @SerializedName("total_pages")
    val totalPages: Int
)

