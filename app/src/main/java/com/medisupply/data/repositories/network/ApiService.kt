package com.medisupply.data.repositories.network

import com.medisupply.data.models.Cliente
import com.medisupply.data.models.LoginRequest
import com.medisupply.data.models.LoginResponse
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.models.UserProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

    @GET("productos")
    suspend fun getProductos(): ProductoResponse

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("auth/me")
    fun getMe(@Header("Authorization") token: String): Call<UserProfileResponse>
}
