package com.medisupply.data.repositories

import android.webkit.MimeTypeMap
import com.medisupply.data.models.RutaVisitaItem
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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

    /**
     * Registra la visita enviando datos MULTIPART (Texto + Archivo opcional).
     * Soporta detección automática de IMAGEN o VIDEO.
     */
    suspend fun registrarVisita(
        visitaId: String,
        detalle: String,
        clienteContacto: String,
        inicio: String,
        fin: String,
        estado: String,
        archivoEvidencia: File?
    ): VisitaDetalle {
        return withContext(Dispatchers.IO) {

            val detallePart = createPartFromString(detalle)
            val contactoPart = createPartFromString(clienteContacto)
            val inicioPart = createPartFromString(inicio)
            val finPart = createPartFromString(fin)
            val estadoPart = createPartFromString(estado)

            var evidenciaPart: MultipartBody.Part? = null
            if (archivoEvidencia != null) {
                val mimeType = getMimeType(archivoEvidencia) ?: "application/octet-stream"
                val requestFile = archivoEvidencia.asRequestBody(mimeType.toMediaTypeOrNull())
                // "evidencia" es el nombre del campo que espera el Backend (Python)
                evidenciaPart = MultipartBody.Part.createFormData("evidencia", archivoEvidencia.name, requestFile)
            }

            val response = apiService.registrarVisita(
                visitaId,
                detallePart,
                contactoPart,
                inicioPart,
                finPart,
                estadoPart,
                evidenciaPart
            )

            if (!response.isSuccessful || response.body() == null) {
                throw Exception("Error al registrar visita. Código: ${response.code()} - ${response.message()}")
            }

            response.body()!!
        }
    }

    /**
     * Helper para crear partes de texto plano
     */
    private fun createPartFromString(value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Obtiene el MIME Type basado en la extensión del archivo (.jpg, .mp4, etc.)
     * Esto es crucial para que el navegador sepa si reproducir el video o mostrar la foto.
     */
    private fun getMimeType(file: File): String? {
        val extension = file.extension
        return if (extension.isNotEmpty()) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        } else {
            null
        }
    }
}