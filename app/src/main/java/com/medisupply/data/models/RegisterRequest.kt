package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Cliente
 */
data class RegisterRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("nit")
    val nit: String,

    @SerializedName("logoUrl")
    val logoUrl: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("id_client")
    val id_client: String? = null
)
data class ClienteRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("nit")
    val nit: String,

    @SerializedName("logoUrl")
    val logoUrl: String,

    @SerializedName("address")
    val address: String
)