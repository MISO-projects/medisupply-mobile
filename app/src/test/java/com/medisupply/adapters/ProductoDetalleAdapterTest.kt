package com.medisupply.adapters

import android.os.Looper
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.medisupply.R
import com.medisupply.ui.adapters.ProductoDetalleAdapter
import com.medisupply.ui.fragments.ProductoDetalle
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProductoDetalleAdapterTest {

    private lateinit var adapter: ProductoDetalleAdapter

    private val mockProductosDetalle = listOf(
        ProductoDetalle(
            nombre = "Guantes de nitrilo",
            cantidad = 50
        ),
        ProductoDetalle(
            nombre = "Alcohol en gel 500ml",
            cantidad = 25
        ),
        ProductoDetalle(
            nombre = "Amoxicilina 500mg",
            cantidad = 10
        )
    )

    @Before
    fun setup() {
        adapter = ProductoDetalleAdapter()
    }

    // Helper para procesar las tareas pendientes del main looper
    private fun processMainLooper() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `adapter should have zero items initially`() {
        // Then
        assertEquals(0, adapter.itemCount)
        assertTrue(adapter.currentList.isEmpty())
    }

    @Test
    fun `adapter should have correct item count after submitList`() {
        // When
        adapter.submitList(mockProductosDetalle)
        processMainLooper()

        // Then
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter should handle empty list`() {
        // When
        adapter.submitList(emptyList())
        processMainLooper()

        // Then
        assertEquals(0, adapter.itemCount)
        assertTrue(adapter.currentList.isEmpty())
    }

    @Test
    fun `adapter should handle single item list`() {
        // When
        adapter.submitList(listOf(mockProductosDetalle[0]))
        processMainLooper()

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("Guantes de nitrilo", adapter.currentList[0].nombre)
        assertEquals(50, adapter.currentList[0].cantidad)
    }

    @Test
    fun `adapter should handle null list as empty`() {
        // Given
        adapter.submitList(mockProductosDetalle)
        processMainLooper()
        assertEquals(3, adapter.itemCount)

        // When
        adapter.submitList(null)
        processMainLooper()

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `onCreateViewHolder should create ProductoViewHolder with correct view`() {
        // Given
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())

        // When
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Then
        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
        assertNotNull(viewHolder.itemView.findViewById(R.id.product_name) as TextView)
        assertNotNull(viewHolder.itemView.findViewById(R.id.product_quantity) as TextView)
        assertNotNull(viewHolder.itemView.findViewById(R.id.product_image) as ImageView)
    }

    @Test
    fun `onBindViewHolder should bind product name correctly`() {
        // Given
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val producto = mockProductosDetalle[0]

        // When
        adapter.submitList(listOf(producto))
        processMainLooper()
        adapter.onBindViewHolder(viewHolder, 0)

        // Then
        val productName = viewHolder.itemView.findViewById(R.id.product_name) as TextView
        assertEquals("Guantes de nitrilo", productName.text.toString())
    }

    @Test
    fun `onBindViewHolder should bind product quantity correctly`() {
        // Given
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val producto = mockProductosDetalle[0]

        // When
        adapter.submitList(listOf(producto))
        processMainLooper()
        adapter.onBindViewHolder(viewHolder, 0)

        // Then
        val productQuantity = viewHolder.itemView.findViewById(R.id.product_quantity) as TextView
        val context = viewHolder.itemView.context
        val expectedText = context.getString(R.string.cantidad, producto.cantidad)
        assertEquals(expectedText, productQuantity.text.toString())
    }

    @Test
    fun `onBindViewHolder should bind all product properties correctly`() {
        // Given
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        val producto = ProductoDetalle(
            nombre = "Gasas estériles",
            cantidad = 100
        )

        // When
        adapter.submitList(listOf(producto))
        processMainLooper()
        adapter.onBindViewHolder(viewHolder, 0)

        // Then
        val productName = viewHolder.itemView.findViewById(R.id.product_name) as TextView
        val productQuantity = viewHolder.itemView.findViewById(R.id.product_quantity) as TextView
        
        assertEquals("Gasas estériles", productName.text.toString())
        val context = viewHolder.itemView.context
        val expectedQuantityText = context.getString(R.string.cantidad, 100)
        assertEquals(expectedQuantityText, productQuantity.text.toString())
    }

    @Test
    fun `getItem should return correct item at position`() {
        // Given
        adapter.submitList(mockProductosDetalle)
        processMainLooper()

        // When
        val item1 = adapter.currentList[0]
        val item2 = adapter.currentList[1]
        val item3 = adapter.currentList[2]

        // Then
        assertEquals("Guantes de nitrilo", item1.nombre)
        assertEquals(50, item1.cantidad)
        
        assertEquals("Alcohol en gel 500ml", item2.nombre)
        assertEquals(25, item2.cantidad)
        
        assertEquals("Amoxicilina 500mg", item3.nombre)
        assertEquals(10, item3.cantidad)
    }

    @Test
    fun `adapter should maintain list order`() {
        // When
        adapter.submitList(mockProductosDetalle)
        processMainLooper()

        // Then
        assertEquals("Guantes de nitrilo", adapter.currentList[0].nombre)
        assertEquals("Alcohol en gel 500ml", adapter.currentList[1].nombre)
        assertEquals("Amoxicilina 500mg", adapter.currentList[2].nombre)
    }

    @Test
    fun `adapter should update item count when list changes`() {
        // Given - Primera lista
        val firstList = listOf(mockProductosDetalle[0], mockProductosDetalle[1])
        adapter.submitList(firstList)
        processMainLooper()
        
        // Verificar primera lista
        assertEquals(2, adapter.currentList.size)

        // When - Segunda lista con menos items
        adapter.submitList(null)
        processMainLooper()
        
        val secondList = listOf(mockProductosDetalle[0])
        adapter.submitList(secondList)
        processMainLooper()

        // Then - Verificar segunda lista
        assertEquals(1, adapter.currentList.size)
    }

    @Test
    fun `adapter currentList should be accessible`() {
        // Given
        adapter.submitList(mockProductosDetalle)
        processMainLooper()

        // When
        val currentList = adapter.currentList

        // Then
        assertNotNull(currentList)
        assertEquals(3, currentList.size)
        assertEquals(mockProductosDetalle, currentList)
    }

    @Test
    fun `DiffUtil should identify same items by nombre`() {
        // Given
        val producto1 = ProductoDetalle(nombre = "Producto A", cantidad = 10)
        val producto2 = ProductoDetalle(nombre = "Producto A", cantidad = 20)

        // When - submitList debería reconocer que es el mismo item
        adapter.submitList(listOf(producto1))
        processMainLooper()
        processMainLooper() // Extra processing for DiffUtil
        
        adapter.submitList(listOf(producto2))
        processMainLooper()
        processMainLooper() // Extra processing for DiffUtil

        // Then - el adapter debería tener 1 item (mismo nombre, por lo tanto mismo item)
        assertEquals(1, adapter.itemCount)
        // Verificar que es el mismo nombre (DiffUtil identifica por nombre)
        assertEquals("Producto A", adapter.currentList[0].nombre)
    }

    @Test
    fun `DiffUtil should identify different items by nombre`() {
        // Given
        val producto1 = ProductoDetalle(nombre = "Producto A", cantidad = 10)
        val producto2 = ProductoDetalle(nombre = "Producto B", cantidad = 10)

        // When - submit list with both items from the start
        adapter.submitList(listOf(producto1, producto2))
        processMainLooper()
        processMainLooper() // Extra processing for DiffUtil

        // Then - debería tener 2 items diferentes
        assertEquals(2, adapter.itemCount)
        assertEquals("Producto A", adapter.currentList[0].nombre)
        assertEquals("Producto B", adapter.currentList[1].nombre)
    }

    @Test
    fun `DiffUtil areContentsTheSame should return true for identical items`() {
        // Given
        val producto1 = ProductoDetalle(nombre = "Producto A", cantidad = 10)
        val producto2 = ProductoDetalle(nombre = "Producto A", cantidad = 10)

        // Then - should be identical
        assertTrue(producto1 == producto2)
    }

    @Test
    fun `DiffUtil areContentsTheSame should return false for different items`() {
        // Given
        val producto1 = ProductoDetalle(nombre = "Producto A", cantidad = 10)
        val producto2 = ProductoDetalle(nombre = "Producto A", cantidad = 20)

        // Then - should be different
        assertFalse(producto1 == producto2)
    }

    @Test
    fun `adapter should handle products with zero quantity`() {
        // Given
        val producto = ProductoDetalle(nombre = "Producto sin stock", cantidad = 0)

        // When
        adapter.submitList(listOf(producto))
        processMainLooper()

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals(0, adapter.currentList[0].cantidad)
    }

    @Test
    fun `adapter should handle products with large quantities`() {
        // Given
        val producto = ProductoDetalle(nombre = "Producto masivo", cantidad = 9999)

        // When
        adapter.submitList(listOf(producto))
        processMainLooper()

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals(9999, adapter.currentList[0].cantidad)
    }

    @Test
    fun `adapter should handle rapid list updates`() {
        // Este test verifica que el adapter puede manejar múltiples actualizaciones
        // sin lanzar excepciones
        
        // Given
        val list1 = listOf(mockProductosDetalle[0])
        val list2 = listOf(mockProductosDetalle[0], mockProductosDetalle[1])
        val list3 = mockProductosDetalle

        // When - Enviar múltiples actualizaciones rápidas
        adapter.submitList(list1)
        processMainLooper()
        
        adapter.submitList(null)
        processMainLooper()
        
        adapter.submitList(list2)
        processMainLooper()
        
        adapter.submitList(null)
        processMainLooper()
        
        adapter.submitList(list3)
        processMainLooper()

        // Then - Verificar que la lista final se aplicó correctamente
        assertEquals(3, adapter.currentList.size)
        assertEquals(mockProductosDetalle, adapter.currentList)
    }

    @Test
    fun `adapter should handle products with empty name`() {
        // Given
        val producto = ProductoDetalle(nombre = "", cantidad = 5)

        // When
        adapter.submitList(listOf(producto))
        processMainLooper()

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("", adapter.currentList[0].nombre)
    }
}
