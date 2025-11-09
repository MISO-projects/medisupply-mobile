package com.medisupply.data.repositories

import com.medisupply.data.models.EntregaProgramadaItem
import com.medisupply.data.repositories.network.ApiService

class EntregasRepository(private val apiService: ApiService) {

    suspend fun obtenerEntregasProgramadas(
        estadoParada: String? = null,
        estadoRuta: String? = null,
        page: Int = 1,
        pageSize: Int = 20
    ): List<EntregaProgramadaItem> {
        return try {
            val response = apiService.getMisEntregasProgramadas(
                estadoParada = estadoParada,
                estadoRuta = estadoRuta,
                page = page,
                pageSize = pageSize
            )
            response.data
        } catch (e: Exception) {
            throw RuntimeException("Error obteniendo entregas programadas de la red", e)
        }
    }
}

