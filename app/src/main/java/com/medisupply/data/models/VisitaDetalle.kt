package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo para la lista anidada de notas anteriores
 */
data class NotaVisita(
    @SerializedName("fecha_visita_programada")
    val fechaVisitaProgramada: String,

    @SerializedName("detalle")
    val detalle: String?
)

data class ProductoPreferido(
    @SerializedName("id_producto")
    val idProducto: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("cantidad_total")
    val cantidadTotal: Int
)

/**
 * Representa la respuesta JSON del endpoint: /visitas/{id}
 */
data class VisitaDetalle(
    @SerializedName("id")
    val id: String,

    @SerializedName("cliente_id")
    val clienteId: String,

    @SerializedName("cliente_contacto")
    val clienteContacto: String?,

    @SerializedName("fecha_visita_programada")
    val fechaVisitaProgramada: String,

    @SerializedName("vendedor_id")
    val vendedorId: String,

    @SerializedName("detalle")
    val detalle: String?,

    @SerializedName("evidencia")
    val evidencia: String?,

    @SerializedName("inicio")
    val inicio: String?,

    @SerializedName("fin")
    val fin: String?,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("created_at")
    val createdAt: String?,

    @SerializedName("updated_at")
    val updatedAt: String?,

    @SerializedName("nombre_institucion")
    val nombreInstitucion: String,

    @SerializedName("direccion")
    val direccion: String,

    @SerializedName("notas_visitas_anteriores")
    val notasVisitasAnteriores: List<NotaVisita>? = null,

    @SerializedName("productos_preferidos")
    val productosPreferidos: List<ProductoPreferido>? = null,

    @SerializedName("tiempo_desplazamiento")
    val tiempoDesplazamiento: String? = null,

    @SerializedName("recomendacion_llm")
    val recomendacionLlm: String? = null
)