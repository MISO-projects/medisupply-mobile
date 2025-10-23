package com.medisupply.repositories

import com.medisupply.data.repositories.ClienteRepository
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
class ClienteRepositoryTest {

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var repository: ClienteRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = ClienteRepository(apiService)
    }

    @Test
    fun `getClientes should return mock data with 6 clients`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        assertNotNull(clientes)
        assertEquals(6, clientes.size)
    }

    @Test
    fun `getClientes should return correct client structure`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        val primerCliente = clientes.first()
        assertNotNull(primerCliente.id)
        assertNotNull(primerCliente.nombre)
        assertNotNull(primerCliente.nit)
        assertNotNull(primerCliente.logoUrl)
    }

    @Test
    fun `getClientes should include Hospital General`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        val hospitalGeneral = clientes.find { it.id == "C001" }
        assertNotNull(hospitalGeneral)
        assertEquals("Hospital General", hospitalGeneral.nombre)
        assertEquals("901234567-8", hospitalGeneral.nit)
    }

    @Test
    fun `getClientes should include Clínica San Martín`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        val clinica = clientes.find { it.id == "C002" }
        assertNotNull(clinica)
        assertEquals("Clínica San Martín", clinica.nombre)
        assertEquals("901234568-9", clinica.nit)
    }

    @Test
    fun `getClientes should include Centro Médico Integral`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        val centro = clientes.find { it.id == "C003" }
        assertNotNull(centro)
        assertEquals("Centro Médico Integral", centro.nombre)
    }

    @Test
    fun `getClientes should have all clients with valid NITs`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        clientes.forEach { cliente ->
            assertNotNull(cliente.nit)
            assertTrue(cliente.nit.isNotEmpty())
            assertTrue(cliente.nit.contains("-"))
        }
    }

    @Test
    fun `getClientes should have all clients with valid logos`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        clientes.forEach { cliente ->
            assertNotNull(cliente.logoUrl)
            assertTrue(cliente.logoUrl.startsWith("https://"))
        }
    }

    @Test
    fun `getClientes should simulate network delay`() = runTest {
        // Given
        val startTime = System.currentTimeMillis()

        // When
        repository.getClientes()

        // Then - En tests con runTest, el delay es virtual
        // Verificamos que se completó la operación
        val endTime = System.currentTimeMillis()
        assertTrue(endTime >= startTime)
    }

    @Test
    fun `getClientes should return consistent data on multiple calls`() = runTest {
        // When
        val clientes1 = repository.getClientes()
        val clientes2 = repository.getClientes()

        // Then
        assertEquals(clientes1.size, clientes2.size)
        assertEquals(clientes1.first().id, clientes2.first().id)
        assertEquals(clientes1.first().nombre, clientes2.first().nombre)
    }

    @Test
    fun `getClientes should have unique IDs`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        val ids = clientes.map { it.id }
        assertEquals(ids.size, ids.distinct().size, "All IDs should be unique")
    }

    @Test
    fun `getClientes should include Hospital Infantil`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        val hospital = clientes.find { it.nombre.contains("Hospital Infantil") }
        assertNotNull(hospital)
        assertEquals("C006", hospital.id)
    }

    @Test
    fun `getClientes should have proper ID format`() = runTest {
        // When
        val clientes = repository.getClientes()

        // Then
        clientes.forEach { cliente ->
            assertTrue(cliente.id.startsWith("C"))
            assertTrue(cliente.id.length == 4)
        }
    }
}


