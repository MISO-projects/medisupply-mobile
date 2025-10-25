package com.medisupply.data.models

import com.google.gson.annotations.SerializedName


data class UserResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("nombre")
    val nombre: String
)


