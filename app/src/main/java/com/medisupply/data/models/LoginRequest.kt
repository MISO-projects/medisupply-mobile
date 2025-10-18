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
    // OJO: Basado en tu respuesta anterior, el campo era "access_token"
    // Asegúrate de que coincida con el JSON real de tu API.
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String
)

// --- Modelo para el Perfil de Usuario ---

data class UserProfileResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("is_active")
    val isActive: Boolean,

    // ✅ CAMBIO CLAVE: Campo de rol opcional
    @SerializedName("role")
    val role: String?
)