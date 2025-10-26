package com.medisupply.repositories

import com.medisupply.data.models.CrearPedidoResponse
import com.medisupply.data.models.ListarPedidosResponse
import com.medisupply.data.models.Pedido
import com.medisupply.data.models.PedidoItem
import com.medisupply.data.models.PedidoRequest
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class PedidoRepositoryTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: PedidoRepository

    private val mockPedidoItems = listOf(
        PedidoItem(
            idProducto = "P001",
            cantidad = 10,
            precioUnitario = 15.50
        ),
        PedidoItem(
            idProducto = "P002",
            cantidad = 5,
            precioUnitario = 25.00
        )
    )

    private val mockPedidos = listOf(
        Pedido(
            id = "ORD001",
            numeroPedido = "ORD-2024-001",
            fechaCreacion = "2024-10-20T10:00:00Z",
            fechaActualizacion = "2024-10-20T10:00:00Z",
            fechaEntregaEstimada = "2024-10-25T10:00:00Z",
            estado = "PENDIENTE",
            valor_total = 280.00,
            clienteId = "C001",
            nombreCliente = "Hospital General",
            vendedorId = "V001",
            creadoPor = "Juan Pérez",
            cantidadItems = 2,
            observaciones = "Entrega urgente",
            productos = mockPedidoItems
        ),
        Pedido(
            id = "ORD002",
            numeroPedido = "ORD-2024-002",
            fechaCreacion = "2024-10-21T14:30:00Z",
            fechaActualizacion = "2024-10-21T14:30:00Z",
            fechaEntregaEstimada = "2024-10-26T14:30:00Z",
            estado = "EN_PROCESO",
            valor_total = 450.00,
            clienteId = "C002",
            nombreCliente = "Clínica San Martín",
            vendedorId = "V001",
            creadoPor = "María López",
            cantidadItems = 3,
            observaciones = null,
            productos = mockPedidoItems
        )
    )

    private val mockPedidoRequest = PedidoRequest(
        clienteId = "C001",
        vendedorId = "V001",
        observaciones = "Entrega urgente",
        productos = mockPedidoItems
    )

    private val mockCrearPedidoResponse = CrearPedidoResponse(
        id = "ORD003",
        numeroPedido = "ORD-2024-003"
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = PedidoRepository(apiService)
    }

    // Tests for crearPedido
    @Test
    fun `crearPedido should return CrearPedidoResponse with valid data`() = runTest {
        // Given
        whenever(apiService.crearPedido(any())).thenReturn(mockCrearPedidoResponse)

        // When
        val response = repository.crearPedido(mockPedidoRequest)

        // Then
        assertNotNull(response)
        assertEquals("ORD003", response.id)
        assertEquals("ORD-2024-003", response.numeroPedido)
    }

    @Test
    fun `crearPedido should return response with valid numero pedido format`() = runTest {
        // Given
        whenever(apiService.crearPedido(any())).thenReturn(mockCrearPedidoResponse)

        // When
        val response = repository.crearPedido(mockPedidoRequest)

        // Then
        assertTrue(response.numeroPedido.startsWith("ORD-"))
        assertTrue(response.numeroPedido.contains("2024"))
    }

    @Test
    fun `crearPedido should throw RuntimeException when network fails`() = runTest {
        // Given
        whenever(apiService.crearPedido(any())).thenThrow(RuntimeException("Network error"))

        // When & Then
        val exception = assertFailsWith<RuntimeException> {
            repository.crearPedido(mockPedidoRequest)
        }
        assertTrue(exception.message?.contains("Error creando pedido") == true)
    }

    @Test
    fun `crearPedido should handle pedido with observaciones`() = runTest {
        // Given
        val pedidoConObservaciones = mockPedidoRequest.copy(
            observaciones = "Entrega urgente - Piso 3"
        )
        whenever(apiService.crearPedido(any())).thenReturn(mockCrearPedidoResponse)

        // When
        val response = repository.crearPedido(pedidoConObservaciones)

        // Then
        assertNotNull(response)
        assertEquals("ORD003", response.id)
    }

    @Test
    fun `crearPedido should handle pedido without observaciones`() = runTest {
        // Given
        val pedidoSinObservaciones = mockPedidoRequest.copy(observaciones = null)
        whenever(apiService.crearPedido(any())).thenReturn(mockCrearPedidoResponse)

        // When
        val response = repository.crearPedido(pedidoSinObservaciones)

        // Then
        assertNotNull(response)
        assertEquals("ORD003", response.id)
    }

    @Test
    fun `crearPedido should handle pedido with multiple productos`() = runTest {
        // Given
        val multipleProductos = listOf(
            PedidoItem("P001", 10, 15.50),
            PedidoItem("P002", 5, 25.00),
            PedidoItem("P003", 20, 8.75)
        )
        val pedidoConMultipleProductos = mockPedidoRequest.copy(productos = multipleProductos)
        whenever(apiService.crearPedido(any())).thenReturn(mockCrearPedidoResponse)

        // When
        val response = repository.crearPedido(pedidoConMultipleProductos)

        // Then
        assertNotNull(response)
        assertEquals("ORD003", response.id)
    }

    // Tests for obtenerPedidoPorId
    @Test
    fun `obtenerPedidoPorId should return pedido with valid data`() = runTest {
        // Given
        val pedidoId = "ORD001"
        whenever(apiService.getPedidoById(pedidoId)).thenReturn(mockPedidos[0])

        // When
        val pedido = repository.obtenerPedidoPorId(pedidoId)

        // Then
        assertNotNull(pedido)
        assertEquals("ORD001", pedido.id)
        assertEquals("ORD-2024-001", pedido.numeroPedido)
        assertEquals("PENDIENTE", pedido.estado)
    }

    @Test
    fun `obtenerPedidoPorId should return pedido with cliente information`() = runTest {
        // Given
        val pedidoId = "ORD001"
        whenever(apiService.getPedidoById(pedidoId)).thenReturn(mockPedidos[0])

        // When
        val pedido = repository.obtenerPedidoPorId(pedidoId)

        // Then
        assertEquals("C001", pedido.clienteId)
        assertEquals("Hospital General", pedido.nombreCliente)
    }

    @Test
    fun `obtenerPedidoPorId should return pedido with productos`() = runTest {
        // Given
        val pedidoId = "ORD001"
        whenever(apiService.getPedidoById(pedidoId)).thenReturn(mockPedidos[0])

        // When
        val pedido = repository.obtenerPedidoPorId(pedidoId)

        // Then
        assertNotNull(pedido.productos)
        assertEquals(2, pedido.productos.size)
        assertEquals("P001", pedido.productos[0].idProducto)
    }

    @Test
    fun `obtenerPedidoPorId should return pedido with correct total value`() = runTest {
        // Given
        val pedidoId = "ORD001"
        whenever(apiService.getPedidoById(pedidoId)).thenReturn(mockPedidos[0])

        // When
        val pedido = repository.obtenerPedidoPorId(pedidoId)

        // Then
        assertEquals(280.00, pedido.valor_total)
    }

    @Test
    fun `obtenerPedidoPorId should return pedido with observaciones when present`() = runTest {
        // Given
        val pedidoId = "ORD001"
        whenever(apiService.getPedidoById(pedidoId)).thenReturn(mockPedidos[0])

        // When
        val pedido = repository.obtenerPedidoPorId(pedidoId)

        // Then
        assertNotNull(pedido.observaciones)
        assertEquals("Entrega urgente", pedido.observaciones)
    }

    @Test
    fun `obtenerPedidoPorId should return pedido with null observaciones when not present`() = runTest {
        // Given
        val pedidoId = "ORD002"
        whenever(apiService.getPedidoById(pedidoId)).thenReturn(mockPedidos[1])

        // When
        val pedido = repository.obtenerPedidoPorId(pedidoId)

        // Then
        assertEquals(null, pedido.observaciones)
    }

    @Test
    fun `obtenerPedidoPorId should throw RuntimeException when network fails`() = runTest {
        // Given
        val pedidoId = "ORD001"
        whenever(apiService.getPedidoById(pedidoId)).thenThrow(RuntimeException("Network error"))

        // When & Then
        val exception = assertFailsWith<RuntimeException> {
            repository.obtenerPedidoPorId(pedidoId)
        }
        assertTrue(exception.message?.contains("Error obteniendo pedido con ID") == true)
        assertTrue(exception.message?.contains(pedidoId) == true)
    }

    // Tests for listarPedidos
    @Test
    fun `listarPedidos should return list of pedidos`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        assertNotNull(pedidos)
        assertEquals(2, pedidos.size)
    }

    @Test
    fun `listarPedidos should return pedidos with correct structure`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        val primerPedido = pedidos.first()
        assertNotNull(primerPedido.id)
        assertNotNull(primerPedido.numeroPedido)
        assertNotNull(primerPedido.estado)
        assertNotNull(primerPedido.nombreCliente)
    }

    @Test
    fun `listarPedidos should return pedidos with different estados`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        val estados = pedidos.map { it.estado }
        assertTrue(estados.contains("PENDIENTE"))
        assertTrue(estados.contains("EN_PROCESO"))
    }

    @Test
    fun `listarPedidos should return pedidos ordered by creation date`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        assertEquals("2024-10-20T10:00:00Z", pedidos[0].fechaCreacion)
        assertEquals("2024-10-21T14:30:00Z", pedidos[1].fechaCreacion)
    }

    @Test
    fun `listarPedidos should return empty list when no pedidos`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 0,
            pedidos = emptyList(),
            page = 1,
            pageSize = 10,
            totalPages = 0
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        assertTrue(pedidos.isEmpty())
    }

    @Test
    fun `listarPedidos should throw RuntimeException when network fails`() = runTest {
        // Given
        whenever(apiService.getPedidos()).thenThrow(RuntimeException("Network error"))

        // When & Then
        val exception = assertFailsWith<RuntimeException> {
            repository.listarPedidos()
        }
        assertTrue(exception.message?.contains("Error obteniendo pedidos") == true)
    }

    @Test
    fun `listarPedidos should return pedidos with unique IDs`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        val ids = pedidos.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "All pedido IDs should be unique")
    }

    @Test
    fun `listarPedidos should return pedidos with valid numero pedido format`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        pedidos.forEach { pedido ->
            assertTrue(pedido.numeroPedido.startsWith("ORD-"))
            assertTrue(pedido.numeroPedido.length > 10)
        }
    }

    @Test
    fun `listarPedidos should return consistent data on multiple calls`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos1 = repository.listarPedidos()
        val pedidos2 = repository.listarPedidos()

        // Then
        assertEquals(pedidos1.size, pedidos2.size)
        assertEquals(pedidos1.first().id, pedidos2.first().id)
        assertEquals(pedidos1.first().numeroPedido, pedidos2.first().numeroPedido)
    }

    @Test
    fun `listarPedidos should return pedidos with vendedor information`() = runTest {
        // Given
        val response = ListarPedidosResponse(
            total = 2,
            pedidos = mockPedidos,
            page = 1,
            pageSize = 10,
            totalPages = 1
        )
        whenever(apiService.getPedidos()).thenReturn(response)

        // When
        val pedidos = repository.listarPedidos()

        // Then
        pedidos.forEach { pedido ->
            assertNotNull(pedido.vendedorId)
            assertNotNull(pedido.creadoPor)
            assertTrue(pedido.vendedorId.isNotEmpty())
        }
    }
}
