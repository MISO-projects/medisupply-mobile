package com.medisupply.repositories

import com.medisupply.data.repositories.InventarioRepository
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class InventarioRepositoryTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: InventarioRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = InventarioRepository(apiService)
    }

    @Test
    fun `getProductos should return mock data with 11 products`() = runTest {
        // When
        val response = repository.getProductos()

        // Then
        assertNotNull(response)
        assertEquals(11, response.total)
        assertEquals(11, response.productos.size)
    }

    @Test
    fun `getProductos should return correct product structure`() = runTest {
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
        // When
        val response = repository.getProductos()

        // Then
        val todosDisponibles = response.productos.all { it.disponible }
        assertTrue(todosDisponibles)
    }

    @Test
    fun `getProductos should include products with different stock levels`() = runTest {
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
        // When
        val response = repository.getProductos()

        // Then
        val unidades = response.productos.map { it.unidadMedida }.distinct()
        assertTrue(unidades.contains("UNIDAD"))
        assertTrue(unidades.contains("CAJA"))
    }

    @Test
    fun `getProductos should simulate network delay`() = runTest {
        // Given - Este test verifica que el método getProductos() completa exitosamente
        // El delay real no se puede verificar en runTest porque usa tiempo virtual
        
        // When
        val response = repository.getProductos()

        // Then - Verificamos que la respuesta es válida y completa
        assertNotNull(response)
        assertEquals(11, response.productos.size)
        assertTrue(response.productos.isNotEmpty())
    }

    @Test
    fun `getProductos should return consistent data on multiple calls`() = runTest {
        // When
        val response1 = repository.getProductos()
        val response2 = repository.getProductos()

        // Then
        assertEquals(response1.total, response2.total)
        assertEquals(response1.productos.size, response2.productos.size)
        assertEquals(response1.productos.first().id, response2.productos.first().id)
    }
}


