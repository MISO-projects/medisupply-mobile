package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para Cliente
 */
data class Cliente(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("nombre")
    val nombre: String,
    
    @SerializedName("nit")
    val nit: String,
    
    @SerializedName("logoUrl")
    val logoUrl: String
)
