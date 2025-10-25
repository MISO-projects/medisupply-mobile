package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

data class PedidoItem(
    @SerializedName("id_producto")
    val idProducto: String,

    @SerializedName("cantidad")
    val cantidad: Int,

    @SerializedName("precio_unitario")
    val precioUnitario: Double,
)

data class Pedido(
    @SerializedName("id")
    val id: String,

    @SerializedName("numero_orden")
    val numeroPedido: String,


    @SerializedName("fecha_creacion")
    val fechaCreacion: String,

    @SerializedName("fecha_actualizacion")
    val fechaActualizacion: String,

    @SerializedName("fecha_entrega_estimada")
    val fechaEntregaEstimada: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("valor_total")
    val valor_total: Double,

    @SerializedName("id_cliente")
    val clienteId: String,

    @SerializedName("nombre_cliente")
    val nombreCliente: String,

    @SerializedName("id_vendedor")
    val vendedorId: String,

    @SerializedName("creado_por")
    val creadoPor: String,

    @SerializedName("cantidad_items")
    val cantidadItems: Int,

    @SerializedName("observaciones")
    val observaciones: String?,

    @SerializedName("detalles")
    val productos: List<PedidoItem>
)

data class PedidoRequest(
    @SerializedName("id_cliente")
    val clienteId: String,

    @SerializedName("id_vendedor")
    val vendedorId: String,

    @SerializedName("creado_por")
    val creadoPor: String,

    @SerializedName("observaciones")
    val observaciones: String?,

    @SerializedName("detalles")
    val productos: List<PedidoItem>
)

data class CrearPedidoResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("numero_orden")
    val numeroPedido: String,
)

data class ListarPedidosResponse(
    @SerializedName("total")
    val total: Int,

    @SerializedName("data")  // Changed from "ordenes" to "data"
    val pedidos: List<Pedido>,

    @SerializedName("page")
    val page: Int?,

    @SerializedName("page_size")
    val pageSize: Int?,

    @SerializedName("total_pages")
    val totalPages: Int?
)

