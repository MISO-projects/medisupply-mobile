package com.medisupply.data.repositories

import com.medisupply.data.models.Producto
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.models.InventarioResponse
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.delay

/**
 * Repositorio para manejar datos de inventario
 */
class InventarioRepository(private val apiService: ApiService) {

    /**
     * Obtiene la lista de productos del inventario
     * Por ahora usa datos mock, pero está preparado para usar el servicio real
     */
    suspend fun getProductos(nombre: String? = null): ProductoResponse {
        return apiService.getProductos(nombre)

    }

suspend fun getInventario(
        page: Int = 1,
        pageSize: Int = 10,
        textSearch: String? = null,
        estado: String? = null,
        categoria: String? = null
): InventarioResponse {
    return apiService.getInventario(page, pageSize, textSearch, estado, categoria)
}


    /**
     * Datos mock para desarrollo
     */
    private fun getMockProductos(): ProductoResponse {
        val productos = listOf(
            Producto(
                id = "fd3fa57f-f9d9-4e04-ba62-7d696574974b",
                nombre = "Alcohol en gel 500ml",
                categoria = "INSUMOS",
                imagenUrl = "https://example.com/images/alcohol-gel.jpg",
                stockDisponible = 450,
                disponible = true,
                precioUnitario = "12.50",
                unidadMedida = "UNIDAD",
                descripcion = "Alcohol en gel desinfectante 70%. Botella de 500ml"
            ),
            Producto(
                id = "b8983788-0666-4a47-abc6-06862fa629e5",
                nombre = "Amoxicilina 500mg",
                categoria = "MEDICAMENTOS",
                imagenUrl = "https://example.com/images/amoxicilina.jpg",
                stockDisponible = 95,
                disponible = true,
                precioUnitario = "35.75",
                unidadMedida = "CAJA",
                descripcion = "Antibiótico de amplio espectro. Caja con 21 cápsulas"
            ),
            Producto(
                id = "cff6d37a-06fc-400b-81b5-594ff6ee1c1f",
                nombre = "Gasas estériles 10x10cm",
                categoria = "INSUMOS",
                imagenUrl = "https://example.com/images/gasas.jpg",
                stockDisponible = 175,
                disponible = true,
                precioUnitario = "38.00",
                unidadMedida = "CAJA",
                descripcion = "Gasas estériles de algodón. Caja con 100 sobres"
            ),
            Producto(
                id = "97e4fd40-0df3-4002-b74d-c39c87b07642",
                nombre = "Guantes de látex talla M",
                categoria = "INSUMOS",
                imagenUrl = "https://example.com/images/guantes-latex.jpg",
                stockDisponible = 320,
                disponible = true,
                precioUnitario = "45.00",
                unidadMedida = "CAJA",
                descripcion = "Guantes desechables de látex. Caja con 100 unidades"
            ),
            Producto(
                id = "08fd48be-33e2-4d24-8916-4606a097c84b",
                nombre = "Ibuprofeno 400mg",
                categoria = "MEDICAMENTOS",
                imagenUrl = "https://example.com/images/ibuprofeno.jpg",
                stockDisponible = 180,
                disponible = true,
                precioUnitario = "22.00",
                unidadMedida = "CAJA",
                descripcion = "Antiinflamatorio no esteroideo. Caja con 50 tabletas"
            ),
            Producto(
                id = "c5f0e10a-ff3e-49db-bc30-eea7d9727e14",
                nombre = "Jeringas 10ml con aguja",
                categoria = "INSUMOS",
                imagenUrl = "https://example.com/images/jeringas.jpg",
                stockDisponible = 200,
                disponible = true,
                precioUnitario = "55.00",
                unidadMedida = "CAJA",
                descripcion = "Jeringas desechables estériles. Caja con 100 unidades"
            ),
            Producto(
                id = "845079d1-df2a-44b9-b8a4-b294c645b729",
                nombre = "Nebulizador ultrasónico",
                categoria = "EQUIPOS",
                imagenUrl = "https://example.com/images/nebulizador.jpg",
                stockDisponible = 28,
                disponible = true,
                precioUnitario = "245.00",
                unidadMedida = "UNIDAD",
                descripcion = "Nebulizador ultrasónico portátil con compresor"
            ),
            Producto(
                id = "05e80de4-71c5-456b-9296-b891021700f8",
                nombre = "Omeprazol 20mg",
                categoria = "MEDICAMENTOS",
                imagenUrl = "https://example.com/images/omeprazol.jpg",
                stockDisponible = 140,
                disponible = true,
                precioUnitario = "28.50",
                unidadMedida = "CAJA",
                descripcion = "Inhibidor de la bomba de protones. Caja con 28 cápsulas"
            ),
            Producto(
                id = "6587e92d-adf8-40e0-ba5e-120ab14941d4",
                nombre = "Paracetamol 500mg",
                categoria = "MEDICAMENTOS",
                imagenUrl = "https://example.com/images/paracetamol.jpg",
                stockDisponible = 250,
                disponible = true,
                precioUnitario = "15.50",
                unidadMedida = "CAJA",
                descripcion = "Analgésico y antipirético. Caja con 100 tabletas"
            ),
            Producto(
                id = "ef4d1f92-0362-44bf-ba12-5905d7f983f6",
                nombre = "Tensiómetro digital de brazo",
                categoria = "EQUIPOS",
                imagenUrl = "https://example.com/images/tensiometro.jpg",
                stockDisponible = 45,
                disponible = true,
                precioUnitario = "185.00",
                unidadMedida = "UNIDAD",
                descripcion = "Tensiómetro digital automático con memoria para 2 usuarios"
            ),
            Producto(
                id = "cc74240c-adff-4c63-bb31-0d04c5e2a376",
                nombre = "Termómetro digital infrarrojo",
                categoria = "EQUIPOS",
                imagenUrl = "https://example.com/images/termometro-infrarrojo.jpg",
                stockDisponible = 68,
                disponible = true,
                precioUnitario = "95.00",
                unidadMedida = "UNIDAD",
                descripcion = "Termómetro sin contacto con lectura en 1 segundo"
            )
        )
        
        return ProductoResponse(
            total = productos.size,
            productos = productos
        )
    }
}

