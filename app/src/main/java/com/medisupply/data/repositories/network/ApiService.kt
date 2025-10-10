package com.medisupply.data.repositories.network

import com.medisupply.data.models.Cliente
import retrofit2.http.GET

interface ApiService {

    @GET("clientes")
    suspend fun getClientes(): List<Cliente>

}
