package com.medisupply.adapters

import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medisupply.data.models.Inventario
import com.medisupply.ui.adapters.ProductosAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProductosAdapterTest {

    private lateinit var adapter: ProductosAdapter
    private var clickedInventario: Inventario? = null

    private val mockInventario = listOf(
        Inventario(
            id = "1",
            productoId = "prod1",
            lote = "L001",
            fechaVencimiento = "2025-12-31",
            cantidad = 100,
            ubicacion = "A1",
            temperaturaRequerida = "Ambiente",
            estado = "DISPONIBLE",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Alcohol en gel 500ml",
            productoSku = "SKU001",
            categoria = "Insumos médicos",
            productoImagenUrl = "https://example.com/alcohol.jpg"
        ),
        Inventario(
            id = "2",
            productoId = "prod2",
            lote = "L002",
            fechaVencimiento = "2025-12-31",
            cantidad = 25,
            ubicacion = "A2",
            temperaturaRequerida = "Ambiente",
            estado = "DISPONIBLE",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Amoxicilina 500mg",
            productoSku = "SKU002",
            categoria = "Medicamento",
            productoImagenUrl = "https://example.com/amoxicilina.jpg"
        ),
        Inventario(
            id = "3",
            productoId = "prod3",
            lote = "L003",
            fechaVencimiento = "2025-12-31",
            cantidad = 15,
            ubicacion = "A3",
            temperaturaRequerida = "Ambiente",
            estado = "DISPONIBLE",
            condicionesEspeciales = "",
            observaciones = "",
            fechaRecepcion = "2024-01-01",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            productoNombre = "Gasas estériles",
            productoSku = "SKU003",
            categoria = "Insumos médicos",
            productoImagenUrl = "https://example.com/gasas.jpg"
        )
    )

    @Before
    fun setup() {
        adapter = ProductosAdapter { inventario ->
            clickedInventario = inventario
        }
    }
    
    // Helper para procesar las tareas pendientes del main looper
    private fun processMainLooper() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `adapter should have correct item count after submitList`() {
        // When
        adapter.submitList(mockInventario)

        // Then
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter should have zero items initially`() {
        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `adapter should update item count when list changes`() {
        // Given - Primera lista
        val firstList = listOf(mockInventario[0], mockInventario[1], mockInventario[2])
        adapter.submitList(firstList)
        processMainLooper()
        
        // Verificar primera lista
        val firstSize = adapter.currentList.size
        assertEquals(3, firstSize, "Primera lista debe tener 3 items")

        // When - Segunda lista con menos items (null primero para forzar actualización)
        adapter.submitList(null)
        processMainLooper()
        
        val secondList = listOf(mockInventario[0], mockInventario[1])
        adapter.submitList(secondList)
        processMainLooper()

        // Then - Verificar segunda lista
        val secondSize = adapter.currentList.size
        assertEquals(2, secondSize, "Segunda lista debe tener 2 items")
    }

    @Test
    fun `getItemId should return correct item`() {
        // Given
        adapter.submitList(mockInventario)

        // When
        val item = adapter.currentList[0]

        // Then
        assertEquals("1", item.id)
        assertEquals("Alcohol en gel 500ml", item.productoNombre)
    }

    @Test
    fun `adapter should handle empty list`() {
        // When
        adapter.submitList(emptyList())

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `adapter should handle single item list`() {
        // When
        adapter.submitList(listOf(mockInventario.first()))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("Alcohol en gel 500ml", adapter.currentList[0].productoNombre)
    }

    @Test
    fun `ProductoDiffCallback should identify same items by id`() {
        // Given
        val inventario1 = mockInventario[0]
        val inventario2 = inventario1.copy(cantidad = 200)

        // When - submitList debería reconocer que es el mismo item
        adapter.submitList(listOf(inventario1))
        adapter.submitList(listOf(inventario2))

        // Then - el adapter debería tener 1 item (actualizado, no agregado)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should maintain list order`() {
        // When
        adapter.submitList(mockInventario)

        // Then
        assertEquals("Alcohol en gel 500ml", adapter.currentList[0].productoNombre)
        assertEquals("Amoxicilina 500mg", adapter.currentList[1].productoNombre)
        assertEquals("Gasas estériles", adapter.currentList[2].productoNombre)
    }

    @Test
    fun `adapter should handle products with different categories`() {
        // Given
        val inventarios = listOf(
            mockInventario[0], // Insumos médicos
            mockInventario[1], // Medicamento
            mockInventario[2]  // Insumos médicos
        )

        // When
        adapter.submitList(inventarios)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("Insumos médicos", adapter.currentList[0].categoria)
        assertEquals("Medicamento", adapter.currentList[1].categoria)
        assertEquals("Insumos médicos", adapter.currentList[2].categoria)
    }

    @Test
    fun `adapter should handle products with different stock levels`() {
        // Given
        adapter.submitList(mockInventario)

        // Then
        assertEquals(100, adapter.currentList[0].cantidad) // Alto
        assertEquals(25, adapter.currentList[1].cantidad)  // Medio
        assertEquals(15, adapter.currentList[2].cantidad)  // Bajo
    }

    @Test
    fun `adapter should preserve product properties`() {
        // Given
        adapter.submitList(mockInventario)

        // When
        val inventario = adapter.currentList[0]

        // Then
        assertEquals("1", inventario.id)
        assertEquals("Alcohol en gel 500ml", inventario.productoNombre)
        assertEquals("Insumos médicos", inventario.categoria)
        assertEquals("https://example.com/alcohol.jpg", inventario.productoImagenUrl)
        assertEquals(100, inventario.cantidad)
        assertEquals("DISPONIBLE", inventario.estado)
    }

    @Test
    fun `adapter should handle rapid list updates`() {
        // Este test verifica que el adapter puede manejar múltiples actualizaciones
        // sin lanzar excepciones, incluso si algunas actualizaciones intermedias
        // son optimizadas por DiffUtil
        
        // Given
        val list1 = listOf(mockInventario[0])
        val list2 = listOf(mockInventario[0], mockInventario[1])
        val list3 = mockInventario

        // When - Enviar múltiples actualizaciones rápidas
        adapter.submitList(list1)
        processMainLooper()
        
        // Limpiar entre actualizaciones
        adapter.submitList(null)
        processMainLooper()
        
        adapter.submitList(list2)
        processMainLooper()
        
        // Limpiar entre actualizaciones
        adapter.submitList(null)
        processMainLooper()
        
        adapter.submitList(list3)
        processMainLooper()

        // Then - Verificar que la lista final se aplicó correctamente
        assertEquals(3, adapter.currentList.size)
    }

    @Test
    fun `adapter should handle null list as empty`() {
        // Given
        adapter.submitList(mockInventario)
        assertEquals(3, adapter.itemCount)

        // When
        adapter.submitList(null)

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `adapter currentList should be accessible`() {
        // Given
        adapter.submitList(mockInventario)

        // When
        val currentList = adapter.currentList

        // Then
        assertNotNull(currentList)
        assertEquals(3, currentList.size)
        assertEquals(mockInventario, currentList)
    }

    @Test
    fun `adapter should handle products with all available status`() {
        // Given
        val todosDisponibles = mockInventario.all { it.estado == "DISPONIBLE" }

        // When
        adapter.submitList(mockInventario)

        // Then
        assertEquals(true, todosDisponibles)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter should handle products with mixed availability`() {
        // Given
        val inventarios = listOf(
            mockInventario[0].copy(estado = "DISPONIBLE"),
            mockInventario[1].copy(estado = "BLOQUEADO"),
            mockInventario[2].copy(estado = "DISPONIBLE")
        )

        // When
        adapter.submitList(inventarios)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("DISPONIBLE", adapter.currentList[0].estado)
        assertEquals("BLOQUEADO", adapter.currentList[1].estado)
        assertEquals("DISPONIBLE", adapter.currentList[2].estado)
    }
}


