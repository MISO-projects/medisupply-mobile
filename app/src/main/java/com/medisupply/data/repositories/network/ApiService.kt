package com.medisupply.data.repositories.network

import com.medisupply.data.models.Cliente
import com.medisupply.data.models.ClienteRequest
import com.medisupply.data.models.LoginRequest
import com.medisupply.data.models.LoginResponse
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.models.RegisterRequest
import com.medisupply.data.models.UserProfileResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @GET("clientes/asignados")
    suspend fun getClientes(): List<Cliente>

    @GET("productos/disponibles/")
    suspend fun getProductos(): ProductoResponse

    @POST("autenticacion/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("autenticacion/me")
    fun getMe(): Call<UserProfileResponse>

    @POST("autenticacion/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<Unit>

    @POST("clientes/")
    suspend fun crearCliente(@Body cliente: ClienteRequest): Response<Cliente>
}
