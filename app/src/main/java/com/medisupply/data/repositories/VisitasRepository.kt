package com.medisupply.data.repositories

import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.network.ApiService

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
}