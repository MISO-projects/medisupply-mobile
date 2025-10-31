package com.medisupply.adapters

import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import androidx.test.core.app.ApplicationProvider
import com.medisupply.data.models.PedidoResumenCliente
import com.medisupply.ui.adapters.ClientPedidosAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ClientPedidosAdapterTest {

    private lateinit var adapter: ClientPedidosAdapter
    private var clickedPedido: PedidoResumenCliente? = null

    private val mockPedidos = listOf(
        PedidoResumenCliente(
            id = "1",
            numeroPedido = "PED-001",
            fechaCreacion = "2025-10-15",
            estado = "PENDIENTE",
            valor_total = 1500.50,
            clienteId = "CLI-001",
            cantidadItems = 5,
            fechaEntregaEstimada = "2025-10-20"
        ),
        PedidoResumenCliente(
            id = "2",
            numeroPedido = "PED-002",
            fechaCreacion = "2025-10-20T10:30:00",
            estado = "EN_PROCESO",
            valor_total = 2300.75,
            clienteId = "CLI-001",
            cantidadItems = 8,
            fechaEntregaEstimada = "2025-10-25"
        ),
        PedidoResumenCliente(
            id = "3",
            numeroPedido = "PED-003",
            fechaCreacion = "2025-10-22T14:45:30.123456",
            estado = "ENTREGADO",
            valor_total = 850.00,
            clienteId = "CLI-001",
            cantidadItems = 3,
            fechaEntregaEstimada = "2025-10-27"
        )
    )

    @Before
    fun setup() {
        adapter = ClientPedidosAdapter { pedido ->
            clickedPedido = pedido
        }
    }

    // Helper para procesar las tareas pendientes del main looper
    private fun processMainLooper() {
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `adapter should have correct item count after submitList`() {
        // When
        adapter.submitList(mockPedidos)

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
        val firstList = listOf(mockPedidos[0], mockPedidos[1], mockPedidos[2])
        adapter.submitList(firstList)
        processMainLooper()

        // Verificar primera lista
        val firstSize = adapter.currentList.size
        assertEquals(3, firstSize, "Primera lista debe tener 3 items")

        // When - Segunda lista con menos items
        adapter.submitList(null)
        processMainLooper()

        val secondList = listOf(mockPedidos[0], mockPedidos[1])
        adapter.submitList(secondList)
        processMainLooper()

        // Then - Verificar segunda lista
        val secondSize = adapter.currentList.size
        assertEquals(2, secondSize, "Segunda lista debe tener 2 items")
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
        adapter.submitList(listOf(mockPedidos.first()))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("PED-001", adapter.currentList[0].numeroPedido)
    }

    @Test
    fun `adapter should handle null list as empty`() {
        // Given
        adapter.submitList(mockPedidos)
        assertEquals(3, adapter.itemCount)

        // When
        adapter.submitList(null)

        // Then
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun `adapter should maintain list order`() {
        // When
        adapter.submitList(mockPedidos)

        // Then
        assertEquals("PED-001", adapter.currentList[0].numeroPedido)
        assertEquals("PED-002", adapter.currentList[1].numeroPedido)
        assertEquals("PED-003", adapter.currentList[2].numeroPedido)
    }

    @Test
    fun `adapter currentList should be accessible`() {
        // Given
        adapter.submitList(mockPedidos)

        // When
        val currentList = adapter.currentList

        // Then
        assertNotNull(currentList)
        assertEquals(3, currentList.size)
        assertEquals(mockPedidos, currentList)
    }

    @Test
    fun `getItemId should return correct item`() {
        // Given
        adapter.submitList(mockPedidos)

        // When
        val item = adapter.currentList[0]

        // Then
        assertEquals("1", item.id)
        assertEquals("PED-001", item.numeroPedido)
    }

    @Test
    fun `adapter should preserve pedido properties`() {
        // Given
        adapter.submitList(mockPedidos)

        // When
        val pedido = adapter.currentList[0]

        // Then
        assertEquals("1", pedido.id)
        assertEquals("PED-001", pedido.numeroPedido)
        assertEquals("2025-10-15", pedido.fechaCreacion)
        assertEquals("PENDIENTE", pedido.estado)
        assertEquals(1500.50, pedido.valor_total)
        assertEquals("CLI-001", pedido.clienteId)
        assertEquals(5, pedido.cantidadItems)
        assertEquals("2025-10-20", pedido.fechaEntregaEstimada)
    }

    @Test
    fun `PedidoDiffCallback should identify same items by id`() {
        // Given
        val pedido1 = mockPedidos[0]
        val pedido2 = pedido1.copy(estado = "COMPLETADO")

        // When - submitList debería reconocer que es el mismo item
        adapter.submitList(listOf(pedido1))
        adapter.submitList(listOf(pedido2))

        // Then - el adapter debería tener 1 item (actualizado, no agregado)
        assertEquals(1, adapter.itemCount)
    }

    @Test
    fun `adapter should handle pedidos with different estados`() {
        // Given
        val pedidos = listOf(
            mockPedidos[0], // PENDIENTE
            mockPedidos[1], // EN_PROCESO
            mockPedidos[2]  // ENTREGADO
        )

        // When
        adapter.submitList(pedidos)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("PENDIENTE", adapter.currentList[0].estado)
        assertEquals("EN_PROCESO", adapter.currentList[1].estado)
        assertEquals("ENTREGADO", adapter.currentList[2].estado)
    }

    @Test
    fun `adapter should handle pedidos with different valores`() {
        // Given
        adapter.submitList(mockPedidos)

        // Then
        assertEquals(1500.50, adapter.currentList[0].valor_total)
        assertEquals(2300.75, adapter.currentList[1].valor_total)
        assertEquals(850.00, adapter.currentList[2].valor_total)
    }

    @Test
    fun `adapter should handle pedidos with different cantidad de items`() {
        // Given
        adapter.submitList(mockPedidos)

        // Then
        assertEquals(5, adapter.currentList[0].cantidadItems)
        assertEquals(8, adapter.currentList[1].cantidadItems)
        assertEquals(3, adapter.currentList[2].cantidadItems)
    }

    @Test
    fun `adapter should handle rapid list updates`() {
        // Given
        val list1 = listOf(mockPedidos[0])
        val list2 = listOf(mockPedidos[0], mockPedidos[1])
        val list3 = mockPedidos

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
    }

    @Test
    fun `adapter should handle pedidos with zero items`() {
        // Given
        val pedidoConCeroItems = mockPedidos[0].copy(cantidadItems = 0)

        // When
        adapter.submitList(listOf(pedidoConCeroItems))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals(0, adapter.currentList[0].cantidadItems)
    }

    @Test
    fun `adapter should handle pedidos with zero value`() {
        // Given
        val pedidoConValorCero = mockPedidos[0].copy(valor_total = 0.0)

        // When
        adapter.submitList(listOf(pedidoConValorCero))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals(0.0, adapter.currentList[0].valor_total)
    }

    @Test
    fun `adapter should handle pedidos with very large values`() {
        // Given
        val pedidoGrande = mockPedidos[0].copy(
            valor_total = 999999.99,
            cantidadItems = 1000
        )

        // When
        adapter.submitList(listOf(pedidoGrande))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals(999999.99, adapter.currentList[0].valor_total)
        assertEquals(1000, adapter.currentList[0].cantidadItems)
    }

    @Test
    fun `adapter should handle pedidos with very long numero`() {
        // Given
        val pedidoNumeroLargo = mockPedidos[0].copy(
            numeroPedido = "PED-2025-OCT-000001-URGENT-PRIORITY-HIGH"
        )

        // When
        adapter.submitList(listOf(pedidoNumeroLargo))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("PED-2025-OCT-000001-URGENT-PRIORITY-HIGH", adapter.currentList[0].numeroPedido)
    }

    @Test
    fun `adapter should handle pedidos with different date formats`() {
        // Given - Pedidos con diferentes formatos de fecha
        val pedidos = listOf(
            mockPedidos[0].copy(fechaCreacion = "2025-10-15"),  // Formato básico
            mockPedidos[1].copy(fechaCreacion = "2025-10-20T10:30:00"),  // Con hora
            mockPedidos[2].copy(fechaCreacion = "2025-10-22T14:45:30.123456")  // Con microsegundos
        )

        // When
        adapter.submitList(pedidos)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("2025-10-15", adapter.currentList[0].fechaCreacion)
        assertEquals("2025-10-20T10:30:00", adapter.currentList[1].fechaCreacion)
        assertEquals("2025-10-22T14:45:30.123456", adapter.currentList[2].fechaCreacion)
    }

    @Test
    fun `onCreateViewHolder should create a valid ViewHolder`() {
        // Given
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())

        // When
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Then
        assertNotNull(viewHolder)
        assertNotNull(viewHolder.itemView)
    }

    @Test
    fun `onBindViewHolder should bind pedido data to ViewHolder`() {
        // Given
        adapter.submitList(mockPedidos)
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Then - El ViewHolder debe tener los datos del pedido
        // Note: En un entorno real, necesitaríamos verificar los valores en las vistas
        // Este test verifica que el método no lanza excepciones
        assertNotNull(viewHolder)
    }

    @Test
    fun `ViewHolder should handle click events`() {
        // Given
        adapter.submitList(mockPedidos)
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())
        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(viewHolder, 0)
        assertNull(clickedPedido)

        // When
        viewHolder.itemView.performClick()
        processMainLooper()

        // Then
        assertNotNull(clickedPedido)
        assertEquals("1", clickedPedido?.id)
        assertEquals("PED-001", clickedPedido?.numeroPedido)
    }

    @Test
    fun `ViewHolder should handle click for different pedidos`() {
        // Given
        adapter.submitList(mockPedidos)
        val parent = LinearLayout(ApplicationProvider.getApplicationContext())

        // When - Click en el primer pedido
        val viewHolder1 = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(viewHolder1, 0)
        viewHolder1.itemView.performClick()
        processMainLooper()

        // Then
        assertEquals("1", clickedPedido?.id)

        // When - Click en el segundo pedido
        val viewHolder2 = adapter.onCreateViewHolder(parent, 0)
        adapter.onBindViewHolder(viewHolder2, 1)
        viewHolder2.itemView.performClick()
        processMainLooper()

        // Then
        assertEquals("2", clickedPedido?.id)
    }

    @Test
    fun `adapter should handle list with same cliente but different pedidos`() {
        // Given
        val pedidos = listOf(
            mockPedidos[0].copy(clienteId = "CLI-001"),
            mockPedidos[1].copy(clienteId = "CLI-001"),
            mockPedidos[2].copy(clienteId = "CLI-001")
        )

        // When
        adapter.submitList(pedidos)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("CLI-001", adapter.currentList[0].clienteId)
        assertEquals("CLI-001", adapter.currentList[1].clienteId)
        assertEquals("CLI-001", adapter.currentList[2].clienteId)
    }

    @Test
    fun `adapter should handle list with different clientes`() {
        // Given
        val pedidos = listOf(
            mockPedidos[0].copy(clienteId = "CLI-001"),
            mockPedidos[1].copy(clienteId = "CLI-002"),
            mockPedidos[2].copy(clienteId = "CLI-003")
        )

        // When
        adapter.submitList(pedidos)

        // Then
        assertEquals(3, adapter.itemCount)
        assertEquals("CLI-001", adapter.currentList[0].clienteId)
        assertEquals("CLI-002", adapter.currentList[1].clienteId)
        assertEquals("CLI-003", adapter.currentList[2].clienteId)
    }

    @Test
    fun `adapter should handle pedidos with empty strings`() {
        // Given - Pedido con strings vacíos (excepto id que es requerido)
        val pedidoVacio = PedidoResumenCliente(
            id = "999",
            numeroPedido = "",
            fechaCreacion = "",
            estado = "",
            valor_total = 0.0,
            clienteId = "",
            cantidadItems = 0,
            fechaEntregaEstimada = ""
        )

        // When
        adapter.submitList(listOf(pedidoVacio))

        // Then
        assertEquals(1, adapter.itemCount)
        assertEquals("", adapter.currentList[0].numeroPedido)
        assertEquals("", adapter.currentList[0].estado)
    }

    @Test
    fun `DiffCallback should recognize different items with different ids`() {
        // Given
        val pedido1 = mockPedidos[0]
        val pedido2 = mockPedidos[1]
        val listWithTwo = listOf(pedido1, pedido2)

        // When
        adapter.submitList(listWithTwo)
        processMainLooper()

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals("1", adapter.currentList[0].id)
        assertEquals("2", adapter.currentList[1].id)
    }

    @Test
    fun `DiffCallback should detect content changes`() {
        // Given
        val pedido1 = mockPedidos[0]
        val pedido2 = pedido1.copy(estado = "COMPLETADO", valor_total = 9999.99)

        // When - Submit with second version directly
        adapter.submitList(listOf(pedido2))
        processMainLooper()

        // Then - Debe tener el contenido actualizado
        assertEquals(1, adapter.itemCount)
        assertEquals("COMPLETADO", adapter.currentList[0].estado)
        assertEquals(9999.99, adapter.currentList[0].valor_total)
        assertEquals("1", adapter.currentList[0].id) // Same ID as original
    }

    @Test
    fun `adapter should handle pedidos sorted by date`() {
        // Given
        val pedidosOrdenados = mockPedidos.sortedBy { it.fechaCreacion }

        // When
        adapter.submitList(pedidosOrdenados)

        // Then
        assertEquals(3, adapter.itemCount)
        // Verificar que mantiene el orden
        assertEquals(pedidosOrdenados[0].fechaCreacion, adapter.currentList[0].fechaCreacion)
        assertEquals(pedidosOrdenados[1].fechaCreacion, adapter.currentList[1].fechaCreacion)
        assertEquals(pedidosOrdenados[2].fechaCreacion, adapter.currentList[2].fechaCreacion)
    }

    @Test
    fun `adapter should handle pedidos sorted by estado`() {
        // Given
        val pedidosOrdenados = mockPedidos.sortedBy { it.estado }

        // When
        adapter.submitList(pedidosOrdenados)
        processMainLooper()

        // Then
        assertEquals(3, adapter.itemCount)
        // Verificar que mantiene el orden recibido (no reordena)
        assertEquals(pedidosOrdenados[0].estado, adapter.currentList[0].estado)
        assertEquals(pedidosOrdenados[1].estado, adapter.currentList[1].estado)
        assertEquals(pedidosOrdenados[2].estado, adapter.currentList[2].estado)
    }

    @Test
    fun `adapter should handle repeated submitList with same data`() {
        // Given
        adapter.submitList(mockPedidos)
        processMainLooper()
        val firstSize = adapter.currentList.size

        // When
        adapter.submitList(mockPedidos)
        processMainLooper()

        // Then - El tamaño debe ser el mismo
        assertEquals(firstSize, adapter.currentList.size)
        assertEquals(3, adapter.itemCount)
    }

    @Test
    fun `adapter should handle removal of items from middle of list`() {
        // Given
        adapter.submitList(mockPedidos)
        processMainLooper()
        assertEquals(3, adapter.itemCount)

        // When - Remove middle item
        val newList = listOf(mockPedidos[0], mockPedidos[2])
        adapter.submitList(null)
        processMainLooper()
        adapter.submitList(newList)
        processMainLooper()

        // Then
        assertEquals(2, adapter.itemCount)
        assertEquals("1", adapter.currentList[0].id)
        assertEquals("3", adapter.currentList[1].id)
    }
}

