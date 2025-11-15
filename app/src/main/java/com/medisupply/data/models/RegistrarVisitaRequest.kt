package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Representa el cuerpo JSON para la petición PUT de registro de visita.
 */
data class RegistroVisitaRequest(
    @SerializedName("detalle")
    val detalle: String,

    @SerializedName("estado")
    val estado: String,

    @SerializedName("evidencia")
    val evidencia: String? = null,

    @SerializedName("cliente_contacto")
    val clienteContacto: String? = null,

    @SerializedName("inicio")
    val inicio: String? = null,

    @SerializedName("fin")
    val fin: String? = null
)