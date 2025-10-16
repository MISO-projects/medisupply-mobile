package com.medisupply.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.medisupply.data.models.Producto
import com.medisupply.ui.adapters.ProductosAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProductosAdapterTest {

    private lateinit var adapter: ProductosAdapter
    private var clickedProducto: Producto? = null

    private val mockProductos = listOf(
        Producto(
            id = "1",
            nombre = "Alcohol en gel 500ml",
            categoria = "INSUMOS",
            imagenUrl = "https://example.com/alcohol.jpg",
            stockDisponible = 100,
            disponible = true,
            precioUnitario = "12.50",
            unidadMedida = "UNIDAD",
            descripcion = "Alcohol desinfectante"
        ),
        Producto(
            id = "2",
            nombre = "Amoxicilina 500mg",
            categoria = "MEDICAMENTOS",
            imagenUrl = "https://example.com/amoxicilina.jpg",
            stockDisponible = 25,
            disponible = true,
            precioUnitario = "35.75",
            unidadMedida = "CAJA",
            descripcion = "Antibiótico"
        ),
        Producto(
            id = "3",
            nombre = "Gasas estériles",
            categoria = "INSUMOS",
            imagenUrl = "https://example.com/gasas.jpg",
            stockDisponible = 15,
            disponible = true,
            precioUnitario = "38.00",
            unidadMedida = "CAJA",
            descripcion = "Gasas de algodón"
        )
    )

    @Before
    fun setup() {
        adapter = ProductosAdapter { producto ->
            clickedProducto = producto
        }
    }

    @Test
    fun `adapter should have correct item count after submitList`() {
        // When
        adapter.submitList(mockProductos)

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
        // Given
        adapter.submitList(mockProductos)
        assertEquals(3, adapter.itemCount)

        // When
        adapter.submitList(mockProductos.take(2))

        // Then
        assertEquals(2, adapter.itemCount)
    }

    @Test
    fun `getItemId should return correct item`() {
        // Given
        adapter.submitList(mockProductos)

        // When
        val item = adapter.currentList[0]

        // Then
        assertEquals("1", item.id)
        assertEquals("Alcohol en gel 500ml", item.nombre)
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
        adapter.submitList(listOf(mockProductos.first()))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("Alcohol en gel 500ml", adapter.currentList[0].nombre)
    }

    @Test
    fun `ProductoDiffCallback should identify same items by id`() {
        // Given
        val producto1 = mockProductos[0]
        val producto2 = producto1.copy(stockDisponible = 200)

        // When - submitList debería reconocer que es el mismo item
        adapter.submitList(listOf(producto1))
        adapter.submitList(listOf(producto2))

        // Then - el adapter debería tener 1 item (actualizado, no agregado)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should maintain list order`() {
        // When
        adapter.submitList(mockProductos)

        // Then
        assertEquals("Alcohol en gel 500ml", adapter.currentList[0].nombre)
        assertEquals("Amoxicilina 500mg", adapter.currentList[1].nombre)
        assertEquals("Gasas estériles", adapter.currentList[2].nombre)
    }

    @Test
    fun `adapter should handle products with different categories`() {
        // Given
        val productos = listOf(
            mockProductos[0], // INSUMOS
            mockProductos[1], // MEDICAMENTOS
            mockProductos[2]  // INSUMOS
        )

        // When
        adapter.submitList(productos)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("INSUMOS", adapter.currentList[0].categoria)
        assertEquals("MEDICAMENTOS", adapter.currentList[1].categoria)
        assertEquals("INSUMOS", adapter.currentList[2].categoria)
    }

    @Test
    fun `adapter should handle products with different stock levels`() {
        // Given
        adapter.submitList(mockProductos)

        // Then
        assertEquals(100, adapter.currentList[0].stockDisponible) // Alto
        assertEquals(25, adapter.currentList[1].stockDisponible)  // Medio
        assertEquals(15, adapter.currentList[2].stockDisponible)  // Bajo
    }

    @Test
    fun `adapter should preserve product properties`() {
        // Given
        adapter.submitList(mockProductos)

        // When
        val producto = adapter.currentList[0]

        // Then
        assertEquals("1", producto.id)
        assertEquals("Alcohol en gel 500ml", producto.nombre)
        assertEquals("INSUMOS", producto.categoria)
        assertEquals("https://example.com/alcohol.jpg", producto.imagenUrl)
        assertEquals(100, producto.stockDisponible)
        assertEquals(true, producto.disponible)
        assertEquals("12.50", producto.precioUnitario)
        assertEquals("UNIDAD", producto.unidadMedida)
        assertEquals("Alcohol desinfectante", producto.descripcion)
    }

    @Test
    fun `adapter should handle rapid list updates`() {
        // Given
        val list1 = mockProductos.take(1)
        val list2 = mockProductos.take(2)
        val list3 = mockProductos

        // When
        adapter.submitList(list1)
        adapter.submitList(list2)
        adapter.submitList(list3)

        // Then
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter should handle null list as empty`() {
        // Given
        adapter.submitList(mockProductos)
        assertEquals(3, adapter.itemCount)

        // When
        adapter.submitList(null)

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `adapter currentList should be accessible`() {
        // Given
        adapter.submitList(mockProductos)

        // When
        val currentList = adapter.currentList

        // Then
        assertNotNull(currentList)
        assertEquals(3, currentList.size)
        assertEquals(mockProductos, currentList)
    }

    @Test
    fun `adapter should handle products with all available status`() {
        // Given
        val todosDisponibles = mockProductos.all { it.disponible }

        // When
        adapter.submitList(mockProductos)

        // Then
        assertEquals(true, todosDisponibles)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter should handle products with mixed availability`() {
        // Given
        val productos = listOf(
            mockProductos[0].copy(disponible = true),
            mockProductos[1].copy(disponible = false),
            mockProductos[2].copy(disponible = true)
        )

        // When
        adapter.submitList(productos)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals(true, adapter.currentList[0].disponible)
        assertEquals(false, adapter.currentList[1].disponible)
        assertEquals(true, adapter.currentList[2].disponible)
    }
}

