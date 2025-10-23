package com.medisupply.data.repositories.network

import com.medisupply.data.models.Cliente
import com.medisupply.data.models.ClienteRequest
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.ListarPedidosResponse
import com.medisupply.data.models.LoginRequest
import com.medisupply.data.models.LoginResponse
import com.medisupply.data.models.PedidoRequest
import com.medisupply.data.models.Pedido
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.models.RegisterRequest
import com.medisupply.data.models.UserProfileResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

    @GET("productos")
    suspend fun getProductos(): ProductoResponse

    @GET("ordenes/")
    suspend fun getPedidos(): ListarPedidosResponse

    @GET("ordenes/{id}")
    suspend fun getPedidoById(@Path("id") id: String): Pedido

    @POST("ordenes/")
    suspend fun crearPedido(@Body pedidoRequest: PedidoRequest): CrearPedidoResponse

    @POST("autenticacion/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("autenticacion/me")
    fun getMe(@Header("Authorization") token: String): Call<UserProfileResponse>

    @POST("autenticacion/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<Unit>

    @POST("clientes")
    suspend fun crearCliente(@Body clienteRequest: ClienteRequest): Response<Unit>
}
