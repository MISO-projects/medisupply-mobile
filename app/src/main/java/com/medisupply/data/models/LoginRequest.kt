package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

// --- Modelos para el Login ---

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String
)


data class UserProfileResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("nombre")
    val nombre: String? = null,

    @SerializedName("is_active")
    val isActive: Boolean? = null,

    @SerializedName("role")
    val role: String?  = null,

    @SerializedName("id_seller")
    val idSeller: String,

    @SerializedName("id_client")
    val idClient: String,
)