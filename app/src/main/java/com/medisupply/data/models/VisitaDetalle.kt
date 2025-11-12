package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

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
    val detalle: String?, // sería "Notas de Visita anterior"

    @SerializedName("estado")
    val estado: String,

    @SerializedName("nombre_institucion")
    val nombreInstitucion: String,

    @SerializedName("direccion")
    val direccion: String
    // Faltan campos del UI:
    // - "Productos Preferidos"
    // - "Tiempo de Desplazamiento"
)