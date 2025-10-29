package com.medisupply.data.repositories

import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.Pedido
import com.medisupply.data.models.PedidoRequest
import com.medisupply.data.models.PedidoResumenCliente
import com.medisupply.data.repositories.network.ApiService

class PedidoRepository(private val apiService: ApiService) {

    suspend fun crearPedido(pedido: PedidoRequest): CrearPedidoResponse {
        try {
            return apiService.crearPedido(pedido)
        } catch (e: Exception) {
            throw RuntimeException("Error creando pedido en la red", e)
        }
    }

    suspend fun obtenerPedidoPorId(id: String): Pedido {
        try {
            return apiService.getPedidoById(id)
        } catch (e: Exception) {
            throw RuntimeException("Error obteniendo pedido con ID $id de la red", e)
        }
    }

    suspend fun listarPedidos(): List<Pedido> {
        return try {
            val response = apiService.getPedidos()
            response.pedidos
        } catch (e: Exception) {
            throw RuntimeException("Error obteniendo pedidos de la red", e)
        }
    }

    suspend fun listarPedidosCliente(
            page: Int = 1,
            pageSize: Int = 10
    ): List<PedidoResumenCliente> {
        return try {
            val response = apiService.getPedidosCliente(page, pageSize)
            response.pedidos
        } catch (e: Exception) {
            throw RuntimeException("Error obteniendo pedidos cliente de la red", e)
        }
    }
}
