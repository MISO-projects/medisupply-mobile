package com.medisupply.data.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

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
) : Serializable

data class ClientesAsignadosResponse(
    @SerializedName("clientes")
    val clientes: List<Cliente>,

    @SerializedName("total")
    val total: Int
)
