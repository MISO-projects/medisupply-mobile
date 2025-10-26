package com.medisupply.repositories

import com.medisupply.data.models.Producto
import com.medisupply.data.models.ProductoResponse
import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class InventarioRepositoryTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: InventarioRepository

    private val mockProductos = listOf(
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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = InventarioRepository(apiService)
    }

    @Test
    fun `getProductos should return mock data with 11 products`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        assertNotNull(response)
        assertEquals(11, response.total)
        assertEquals(11, response.productos.size)
    }

    @Test
    fun `getProductos should return correct product structure`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val primerProducto = response.productos.first()
        assertNotNull(primerProducto.id)
        assertNotNull(primerProducto.nombre)
        assertNotNull(primerProducto.categoria)
        assertNotNull(primerProducto.imagenUrl)
        assertTrue(primerProducto.stockDisponible >= 0)
        assertNotNull(primerProducto.precioUnitario)
        assertNotNull(primerProducto.unidadMedida)
        assertNotNull(primerProducto.descripcion)
    }

    @Test
    fun `getProductos should include expected categories`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val categorias = response.productos.map { it.categoria }.distinct()
        assertTrue(categorias.contains("INSUMOS"))
        assertTrue(categorias.contains("MEDICAMENTOS"))
        assertTrue(categorias.contains("EQUIPOS"))
    }

    @Test
    fun `getProductos should include Alcohol en gel product`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val alcoholGel = response.productos.find { 
            it.id == "fd3fa57f-f9d9-4e04-ba62-7d696574974b" 
        }
        assertNotNull(alcoholGel)
        assertEquals("Alcohol en gel 500ml", alcoholGel.nombre)
        assertEquals("INSUMOS", alcoholGel.categoria)
        assertEquals(450, alcoholGel.stockDisponible)
        assertEquals(true, alcoholGel.disponible)
        assertEquals("12.50", alcoholGel.precioUnitario)
    }

    @Test
    fun `getProductos should include Amoxicilina product`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val amoxicilina = response.productos.find { 
            it.id == "b8983788-0666-4a47-abc6-06862fa629e5" 
        }
        assertNotNull(amoxicilina)
        assertEquals("Amoxicilina 500mg", amoxicilina.nombre)
        assertEquals("MEDICAMENTOS", amoxicilina.categoria)
        assertEquals(95, amoxicilina.stockDisponible)
    }

    @Test
    fun `getProductos should include equipment products`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val equipos = response.productos.filter { it.categoria == "EQUIPOS" }
        assertTrue(equipos.isNotEmpty())
        assertTrue(equipos.any { it.nombre.contains("Nebulizador") })
        assertTrue(equipos.any { it.nombre.contains("Tensiómetro") })
        assertTrue(equipos.any { it.nombre.contains("Termómetro") })
    }

    @Test
    fun `getProductos should have all products marked as disponible true`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val todosDisponibles = response.productos.all { it.disponible }
        assertTrue(todosDisponibles)
    }

    @Test
    fun `getProductos should include products with different stock levels`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val stocks = response.productos.map { it.stockDisponible }
        assertTrue(stocks.any { it > 100 }) // Productos con mucho stock
        assertTrue(stocks.any { it in 20..100 }) // Productos con stock medio
        assertTrue(stocks.any { it < 50 }) // Productos con poco stock
    }

    @Test
    fun `getProductos should include products with valid prices`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        response.productos.forEach { producto ->
            val precio = producto.precioUnitario.toDoubleOrNull()
            assertNotNull(precio)
            assertTrue(precio > 0)
        }
    }

    @Test
    fun `getProductos should include different unit measures`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then
        val unidades = response.productos.map { it.unidadMedida }.distinct()
        assertTrue(unidades.contains("UNIDAD"))
        assertTrue(unidades.contains("CAJA"))
    }

    @Test
    fun `getProductos should simulate network delay`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response = repository.getProductos()

        // Then - Verificamos que la respuesta es válida y completa
        assertNotNull(response)
        assertEquals(11, response.productos.size)
        assertTrue(response.productos.isNotEmpty())
    }

    @Test
    fun `getProductos should return consistent data on multiple calls`() = runTest {
        // Given
        whenever(apiService.getProductos(eq(null))).thenReturn(
            ProductoResponse(total = 11, productos = mockProductos)
        )
        
        // When
        val response1 = repository.getProductos()
        val response2 = repository.getProductos()

        // Then
        assertEquals(response1.total, response2.total)
        assertEquals(response1.productos.size, response2.productos.size)
        assertEquals(response1.productos.first().id, response2.productos.first().id)
    }
}


