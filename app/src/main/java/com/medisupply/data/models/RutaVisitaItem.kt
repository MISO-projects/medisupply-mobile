package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos que representa una visita en la lista de rutas.
 * Coincide con la respuesta del BFF: GET /visitas/rutas-del-dia
 */
data class RutaVisitaItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("cliente_id")
    val clienteId: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("direccion")
    val direccion: String?,

    @SerializedName("hora_de_la_cita")
    val horaDeLaCita: String,

    @SerializedName("estado")
    val estado: String // "PENDIENTE", "TOMADA", "CANCELADA"
)