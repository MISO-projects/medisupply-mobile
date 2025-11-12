package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Representa el cuerpo JSON para la petición PUT de registro de visita.
 */
data class RegistroVisitaRequest(
    @SerializedName("detalle")
    val detalle: String,

    @SerializedName("evidencia")
    val evidencia: String,

    @SerializedName("cliente_contacto")
    val clienteContacto: String,

    @SerializedName("inicio")
    val inicio: String,

    @SerializedName("fin")
    val fin: String,

    @SerializedName("estado")
    val estado: String
)