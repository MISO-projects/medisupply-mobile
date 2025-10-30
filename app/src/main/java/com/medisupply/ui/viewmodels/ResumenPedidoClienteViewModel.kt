package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.PedidoClienteRequest
import com.medisupply.data.models.PedidoItem
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.adapters.ProductoConCantidad
import kotlinx.coroutines.launch

class ResumenPedidoClienteViewModel(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _productos = MutableLiveData<List<ProductoConCantidad>>()
    val productos: LiveData<List<ProductoConCantidad>> = _productos

    private val _observaciones = MutableLiveData<String?>()
    val observaciones: LiveData<String?> = _observaciones

    private val _subtotal = MutableLiveData<Double>()
    val subtotal: LiveData<Double> = _subtotal

    private val _impuestos = MutableLiveData<Double>()
    val impuestos: LiveData<Double> = _impuestos

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _pedidoCreado = MutableLiveData<CrearPedidoResponse?>()
    val pedidoCreado: LiveData<CrearPedidoResponse?> = _pedidoCreado

    fun setOrderData(
        productos: List<ProductoConCantidad>,
        observaciones: String?
    ) {
        _productos.value = productos
        _observaciones.value = observaciones

        calculateCosts(productos)
    }

    fun updateObservaciones(observaciones: String?) {
        _observaciones.value = observaciones
    }

    private fun calculateCosts(productos: List<ProductoConCantidad>) {
        val subtotalValue = productos.sumOf { 
            it.producto.precioUnitario.toDouble() * it.cantidad 
        }
        _subtotal.value = subtotalValue

        val impuestosValue = subtotalValue * 0.10 // 10% tax
        _impuestos.value = impuestosValue

        _total.value = subtotalValue + impuestosValue
    }

    fun confirmarPedido() {
        val productosValue = _productos.value

        if (productosValue.isNullOrEmpty()) {
            _error.value = "Datos de pedido incompletos"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true

                val items = productosValue.map { productoConCantidad ->
                    PedidoItem(
                        idProducto = productoConCantidad.producto.id,
                        cantidad = productoConCantidad.cantidad,
                        precioUnitario = productoConCantidad.producto.precioUnitario.toDouble()
                    )
                }

                val pedidoRequest = PedidoClienteRequest(
                    observaciones = _observaciones.value,
                    productos = items
                )

                val response = pedidoRepository.crearPedidoCliente(pedidoRequest)
                _pedidoCreado.value = response
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al crear pedido: ${e.message}"
                _pedidoCreado.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}

