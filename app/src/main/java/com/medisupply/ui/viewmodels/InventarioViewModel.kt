package com.medisupply.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medisupply.data.models.Producto
import com.medisupply.data.repositories.InventarioRepository
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Inventario
 */
class InventarioViewModel(private val inventarioRepository: InventarioRepository) : ViewModel() {

    private val _productos = MutableLiveData<List<Producto>>()
    val productos: LiveData<List<Producto>> = _productos

    private val _productosFiltrados = MutableLiveData<List<Producto>>()
    val productosFiltrados: LiveData<List<Producto>> = _productosFiltrados

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _categorias = MutableLiveData<List<String>>()
    val categorias: LiveData<List<String>> = _categorias

    private var categoriaSeleccionada: String? = null
    private var disponibilidadSeleccionada: Boolean? = null
    private var textoBusqueda: String = ""

    init {
        loadProductos()
    }

    /**
     * Carga la lista de productos
     */
    fun loadProductos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = inventarioRepository.getProductos()
                _productos.value = response.productos
                
                // Extraer categorías únicas
                val categoriasUnicas = response.productos.map { it.categoria }.distinct().sorted()
                _categorias.value = categoriasUnicas
                
                // Inicializar productos filtrados con todos los productos
                _productosFiltrados.value = response.productos
                
            } catch (e: Exception) {
                _error.value = "Error al cargar productos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Filtra productos por búsqueda de texto
     */
    fun buscarProductos(query: String) {
        textoBusqueda = query.trim()
        aplicarFiltros()
    }

    /**
     * Filtra productos por categoría
     */
    fun filtrarPorCategoria(categoria: String?) {
        categoriaSeleccionada = categoria
        aplicarFiltros()
    }

    /**
     * Filtra productos por disponibilidad
     */
    fun filtrarPorDisponibilidad(disponible: Boolean?) {
        disponibilidadSeleccionada = disponible
        aplicarFiltros()
    }

    /**
     * Aplica todos los filtros activos
     */
    private fun aplicarFiltros() {
        val productosActuales = _productos.value ?: return
        
        var productosFiltradosTemp = productosActuales
        
        // Filtrar por texto de búsqueda
        if (textoBusqueda.isNotEmpty()) {
            productosFiltradosTemp = productosFiltradosTemp.filter { producto ->
                producto.nombre.contains(textoBusqueda, ignoreCase = true)
            }
        }
        
        // Filtrar por categoría
        if (categoriaSeleccionada != null) {
            productosFiltradosTemp = productosFiltradosTemp.filter { producto ->
                producto.categoria == categoriaSeleccionada
            }
        }
        
        // Filtrar por disponibilidad
        if (disponibilidadSeleccionada != null) {
            productosFiltradosTemp = productosFiltradosTemp.filter { producto ->
                producto.disponible == disponibilidadSeleccionada
            }
        }
        
        _productosFiltrados.value = productosFiltradosTemp
    }

    /**
     * Limpia todos los filtros
     */
    fun limpiarFiltros() {
        textoBusqueda = ""
        categoriaSeleccionada = null
        disponibilidadSeleccionada = null
        _productosFiltrados.value = _productos.value
    }

    /**
     * Reintenta cargar los productos
     */
    fun retry() {
        loadProductos()
    }
}

