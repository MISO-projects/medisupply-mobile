package com.medisupply.data.repositories.network

import com.medisupply.data.models.Cliente
import com.medisupply.data.models.ProductoResponse
import retrofit2.http.GET

interface ApiService {

    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

    @GET("productos")
    suspend fun getProductos(): ProductoResponse

}
