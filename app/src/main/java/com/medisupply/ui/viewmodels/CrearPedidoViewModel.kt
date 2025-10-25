package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.Cliente
import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.PedidoItem
import com.medisupply.data.models.PedidoRequest
import com.medisupply.data.models.Producto
import com.medisupply.data.repositories.ClienteRepository
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.ui.adapters.ProductoConCantidad
import kotlinx.coroutines.launch

class CrearPedidoViewModel(
    private val clienteRepository: ClienteRepository,
    private val inventarioRepository: InventarioRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _clientes = MutableLiveData<List<Cliente>>()
    val clientes: LiveData<List<Cliente>> = _clientes

    private val _searchResults = MutableLiveData<List<Producto>>()
    val searchResults: LiveData<List<Producto>> = _searchResults

    private val _selectedProductos = MutableLiveData<List<ProductoConCantidad>>(emptyList())
    val selectedProductos: LiveData<List<ProductoConCantidad>> = _selectedProductos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _pedidoCreado = MutableLiveData<CrearPedidoResponse?>()
    val pedidoCreado: LiveData<CrearPedidoResponse?> = _pedidoCreado

    init {
        loadClientes()
    }

    private fun loadClientes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val clientesList = clienteRepository.getClientes()
                _clientes.value = clientesList
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar clientes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProductos(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = inventarioRepository.getProductos(query)
                _searchResults.value = response.productos
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al buscar productos: ${e.message}"
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addProducto(producto: Producto) {
        val currentList = _selectedProductos.value.orEmpty().toMutableList()
        
        // Check if product already exists
        val existingProduct = currentList.find { it.producto.id == producto.id }
        if (existingProduct == null) {
            currentList.add(ProductoConCantidad(producto, 1))
            _selectedProductos.value = currentList
        }
    }

    fun removeProducto(productoId: String) {
        val currentList = _selectedProductos.value.orEmpty().toMutableList()
        currentList.removeAll { it.producto.id == productoId }
        _selectedProductos.value = currentList
    }

    fun updateProductoCantidad(productoConCantidad: ProductoConCantidad) {
        val currentList = _selectedProductos.value.orEmpty().toMutableList()
        val index = currentList.indexOfFirst { it.producto.id == productoConCantidad.producto.id }
        
        if (index != -1) {
            if (productoConCantidad.cantidad <= 0) {
                currentList.removeAt(index)
            } else {
                currentList[index] = productoConCantidad
            }
            _selectedProductos.value = currentList
        }
    }

    fun crearPedido(
        clienteId: String,
        vendedorId: String,
        observaciones: String?
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Filtrar solo productos con cantidad > 0
                val productosConCantidad = _selectedProductos.value.orEmpty().filter { it.cantidad > 0 }
                
                if (productosConCantidad.isEmpty()) {
                    _error.value = "Debe agregar al menos un producto al pedido"
                    _isLoading.value = false
                    return@launch
                }

                val items = productosConCantidad.map { productoConCantidad ->
                    PedidoItem(
                        idProducto = productoConCantidad.producto.id,
                        cantidad = productoConCantidad.cantidad,
                        precioUnitario = productoConCantidad.producto.precioUnitario.toDouble()
                    )
                }

                val pedidoRequest = PedidoRequest(
                    clienteId = clienteId,
                    vendedorId = vendedorId,
                    observaciones = observaciones,
                    productos = items
                )

                val response = pedidoRepository.crearPedido(pedidoRequest)
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
