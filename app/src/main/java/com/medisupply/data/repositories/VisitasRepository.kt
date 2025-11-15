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
    suspend fun getRutasDelDia(
        fecha: String,
        vendedorId: String,
        lat: Double?,
        lon: Double?
    ): List<RutaVisitaItem> {
        return apiService.getRutasDelDia(
            fecha = fecha,
            vendedorId = vendedorId,
            lat = lat,
            lon = lon
        )
    }

    /**
     * Obtiene el detalle de una visita, pasando opcionalmente la ubicación
     * para calcular el tiempo de viaje.
     */
    suspend fun getVisitaById(id: String, lat: Double?, lon: Double?): VisitaDetalle {
        // Pasa los nuevos parámetros al ApiService
        return apiService.getVisitaById(id, lat, lon)
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