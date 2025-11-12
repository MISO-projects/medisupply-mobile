package com.medisupply.data.repositories

import com.medisupply.data.models.RegistroVisitaRequest
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio para manejar datos de las visitas
 */
class VisitasRepository(private val apiService: ApiService) {

    /**
     * Obtiene la lista de rutas de visita desde el BFF
     */
    suspend fun getRutasDelDia(fecha: String, vendedorId: String): List<RutaVisitaItem> {
        // En un futuro, aquí puedes añadir lógica de caché
        return apiService.getRutasDelDia(fecha, vendedorId)
    }

    suspend fun getVisitaById(id: String): VisitaDetalle {
        return apiService.getVisitaById(id)
    }

    suspend fun registrarVisita(visitaId: String, data: RegistroVisitaRequest): VisitaDetalle { 
        return withContext(Dispatchers.IO) {
            val response = apiService.registrarVisita(visitaId, data)

            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Error al registrar visita. Código: ${response.code()}")
            }

            response.body()!!
        }
    }
}