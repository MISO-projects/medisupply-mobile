package com.medisupply.data.repositories.network

import android.icu.text.StringSearch
import com.medisupply.data.models.Cliente
import com.medisupply.data.models.ClienteRequest
import com.medisupply.data.models.ClientesAsignadosResponse
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.EntregasProgramadasResponse
import com.medisupply.data.models.InventarioResponse
import com.medisupply.data.models.ListarPedidosResponse
import com.medisupply.data.models.ListarPedidosResumenClienteResponse
import com.medisupply.data.models.LoginRequest
import com.medisupply.data.models.LoginResponse
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.models.Pedido
import com.medisupply.data.models.PedidoClienteRequest
import com.medisupply.data.models.PedidoPorIdResponse
import com.medisupply.data.models.PedidoRequest
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.models.RegisterRequest
import com.medisupply.data.models.RegistroVisitaRequest
import com.medisupply.data.models.UserProfileResponse
import com.medisupply.data.models.VisitaDetalle
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("clientes/asignados") suspend fun getClientesAsignados(): ClientesAsignadosResponse

    @GET("productos/disponibles")
    suspend fun getProductos(@Query("nombre") nombre: String? = null): ProductoResponse

    @GET("inventario/")
    suspend fun getInventario(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10,
        @Query("text_search") textSearch: String? = null,
        @Query("estado") estado: String? = null,
        @Query("categoria") categoria: String? = null,
    ): InventarioResponse

    @GET("ordenes/") suspend fun getPedidos(): ListarPedidosResponse

    @GET("ordenes/mis-ordenes")
    suspend fun getPedidosCliente(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ListarPedidosResumenClienteResponse

    @GET("ordenes/{id}") suspend fun getPedidoById(@Path("id") id: String): PedidoPorIdResponse

    @POST("ordenes/")
    suspend fun crearPedido(@Body pedidoRequest: PedidoRequest): CrearPedidoResponse

    @POST("ordenes/cliente")
    suspend fun crearPedidoCliente(@Body pedidoRequest: PedidoClienteRequest): CrearPedidoResponse

    @POST("autenticacion/login") fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("autenticacion/me")
    fun getMe(@Header("Authorization") token: String): Call<UserProfileResponse>

    @POST("autenticacion/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<Unit>

    @POST("clientes/") suspend fun crearCliente(@Body cliente: ClienteRequest): Response<Cliente>

    @GET("visitas/rutas-del-dia/")
    suspend fun getRutasDelDia(
        @Query("fecha") fecha: String,
        @Query("vendedor_id") vendedorId: String,
        @Query("lat_actual") lat: Double?,
        @Query("lon_actual") lon: Double?
    ): List<RutaVisitaItem>

    @GET("visitas/{id}")
    suspend fun getVisitaById(
        @Path("id") id: String,
        @Query("lat_actual") lat: Double?,
        @Query("lon_actual") lon: Double?
    ): VisitaDetalle

    @PUT("visitas/{id}")
    suspend fun registrarVisita(
        @Path("id") visitaId: String,
        @Body body: RegistroVisitaRequest
    ): Response<VisitaDetalle>
    @GET("movil/ordenes/mis-entregas-programadas")
    suspend fun getMisEntregasProgramadas(
        @Query("estado_parada") estadoParada: String? = null,
        @Query("estado_ruta") estadoRuta: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): EntregasProgramadasResponse
}
