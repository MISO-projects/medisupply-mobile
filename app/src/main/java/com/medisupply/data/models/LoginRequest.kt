package com.medisupply.data.models

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("user")
    val user: User
)

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)