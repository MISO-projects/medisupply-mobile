package com.medisupply.data.repositories

import com.medisupply.data.models.Cliente
import com.medisupply.data.repositories.network.ApiService
import kotlinx.coroutines.delay

class ClienteRepository(private val apiService: ApiService) {

    suspend fun getClientes(): List<Cliente> {
        delay(1000)
        
        // return apiService.getClientes()

        return getMockClientes()
    }

    /**
     * Datos mock para desarrollo
     */
    private fun getMockClientes(): List<Cliente> {
        return listOf(
            Cliente(
                id = "C001",
                nombre = "Hospital General",
                nit = "901234567-8",
                logoUrl = "https://storage.googleapis.com/logos/hospital-general.png"
            ),
            Cliente(
                id = "C002",
                nombre = "Clínica San Martín",
                nit = "901234568-9",
                logoUrl = "https://storage.googleapis.com/logos/clinica-san-martin.png"
            ),
            Cliente(
                id = "C003",
                nombre = "Centro Médico Integral",
                nit = "901234569-0",
                logoUrl = "https://storage.googleapis.com/logos/centro-medico-integral.png"
            ),
            Cliente(
                id = "C004",
                nombre = "Consultorio Dr. Herrera",
                nit = "901234570-1",
                logoUrl = "https://storage.googleapis.com/logos/consultorio-herrera.png"
            ),
            Cliente(
                id = "C005",
                nombre = "Clínica del Corazón",
                nit = "901234571-2",
                logoUrl = "https://storage.googleapis.com/logos/clinica-corazon.png"
            ),
            Cliente(
                id = "C006",
                nombre = "Hospital Infantil",
                nit = "901234572-3",
                logoUrl = "https://storage.googleapis.com/logos/hospital-infantil.png"
            )
        )
    }
}
