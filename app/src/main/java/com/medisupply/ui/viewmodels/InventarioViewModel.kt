package com.medisupply.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.medisupply.R
import com.medisupply.data.models.Inventario
import com.medisupply.data.repositories.InventarioRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de Inventario
 */
class InventarioViewModel(
    application: Application,
    private val inventarioRepository: InventarioRepository
) : AndroidViewModel(application) {

    private val _productosFiltrados = MutableLiveData<List<Inventario>>()
    val productosFiltrados: LiveData<List<Inventario>> = _productosFiltrados

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _categorias = MutableLiveData<List<String>>()
    val categorias: LiveData<List<String>> = _categorias

    private var categoriaSeleccionada: String? = null
    private var disponibilidadSeleccionada: String? = null
    private var textoBusqueda: String = ""
    
    private var searchJob: Job? = null
    private var hasLoadedData = false
    
    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MS = 500L // 500ms de delay para debounce
        private val CATEGORIAS_STRING_IDS = listOf(
            R.string.categoria_medicamento,
            R.string.categoria_insumos_medicos,
            R.string.categoria_equipamiento,
            R.string.categoria_dispositivos,
            R.string.categoria_consumibles,
            R.string.categoria_material_quirurgico,
            R.string.categoria_reactivos,
            R.string.categoria_otros
        )
    }

    /**
     * Carga la lista de productos con los filtros aplicados
     */
    private fun loadProductos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val response = inventarioRepository.getInventario(
                    page = 1,
                    pageSize = 20, 
                    textSearch = textoBusqueda.takeIf { it.isNotEmpty() },
                    estado = disponibilidadSeleccionada,
                    categoria = categoriaSeleccionada
                )
                
                // Usar lista fija de categorías desde recursos de strings
                val categoriasStrings = CATEGORIAS_STRING_IDS.map { 
                    getApplication<Application>().getString(it) 
                }
                _categorias.value = categoriasStrings
                
                // Actualizar productos filtrados con la respuesta del backend
                _productosFiltrados.value = response.items
                hasLoadedData = true
                
            } catch (e: Exception) {
                _error.value = "Error al cargar productos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadProductosIfNeeded() {
        if (!hasLoadedData) {
            loadProductos()
        }
    }
    
    fun hasDataLoaded(): Boolean {
        val hasData = hasLoadedData || (productosFiltrados.value != null && productosFiltrados.value!!.isNotEmpty())
        return hasData
    }

    /**
     * Filtra productos por búsqueda de texto con debounce
     */
    fun buscarProductos(query: String) {
        textoBusqueda = query.trim()
        
        searchJob?.cancel()
        
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY_MS)
            loadProductos()
        }
    }

    /**
     * Filtra productos por categoría
     */
    fun filtrarPorCategoria(categoria: String?) {
        categoriaSeleccionada = categoria
        loadProductos()
    }

    /**
     * Filtra productos por disponibilidad
     */
    fun filtrarPorDisponibilidad(disponible: String?) {
        disponibilidadSeleccionada = disponible
        loadProductos()
    }

    /**
     * Limpia todos los filtros
     */
    fun limpiarFiltros() {
        textoBusqueda = ""
        categoriaSeleccionada = null
        disponibilidadSeleccionada = null
        loadProductos()
    }

    /**
     * Reintenta cargar los productos
     */
    fun retry() {
        loadProductos()
    }
}

