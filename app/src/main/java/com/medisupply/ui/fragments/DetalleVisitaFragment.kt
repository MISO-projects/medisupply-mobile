package com.medisupply.ui.fragments
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.medisupply.R
import com.medisupply.data.models.VisitaDetalle
import com.medisupply.data.repositories.VisitasRepository
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentDetalleVisitaBinding
import com.medisupply.databinding.ItemDetalleFilaBinding
import com.medisupply.ui.viewmodels.DetalleVisitaViewModel
import com.medisupply.ui.viewmodels.DetalleVisitaViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class DetalleVisitaFragment : Fragment() {

    private var _binding: FragmentDetalleVisitaBinding? = null
    private val binding get() = _binding!!

    private var visitaId: String? = null

    private val viewModel: DetalleVisitaViewModel by viewModels {
        val repository = VisitasRepository(NetworkServiceAdapter.getApiService())
        DetalleVisitaViewModelFactory(repository, visitaId!!)
    }

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val notaDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        private const val ARG_VISITA_ID = "visita_id"

        fun newInstance(id: String): DetalleVisitaFragment {
            return DetalleVisitaFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_VISITA_ID, id)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { visitaId = it.getString(ARG_VISITA_ID) }

        setFragmentResultListener("request_cancelar_visita") { _, bundle ->
            val motivo = bundle.getString("motivo")
            if (!motivo.isNullOrEmpty()) {
                viewModel.cancelarVisita(motivo)
            }
        }

        setFragmentResultListener("request_refresh") { _, bundle ->
            val shouldRefresh = bundle.getBoolean("should_refresh", false)
            if (shouldRefresh) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detalle_visita, container, false)
        return binding.root
    }

    private fun obtenerCoordenadas(direccionCompleta: String?): LatLng? {
        if (direccionCompleta.isNullOrEmpty()) return null

        try {
            val partes = direccionCompleta.split(",")
            if (partes.size >= 2) {
                val lat = partes[0].trim().toDouble()
                val lng = partes[1].trim().toDouble()

                return LatLng(lat, lng)
            }
        } catch (e: Exception) {
            // Si falla el parseo (ej: era "NA", texto basura o formato incorrecto) retornamos null
            return null
        }
        return null
    }

    private fun limpiarDireccion(direccionCompleta: String?): String {
        if (direccionCompleta.isNullOrEmpty()) return "N/A"
        val partes = direccionCompleta.split(",")
        if (partes.size > 2) {
            return partes.subList(2, partes.size).joinToString(",")
        }
        return direccionCompleta
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (visitaId == null) {
            Toast.makeText(requireContext(), "Error: ID no encontrado", Toast.LENGTH_LONG).show()
            parentFragmentManager.popBackStack()
            return
        }
        setupToolbar()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setupObservers() {
        viewModel.visita.observe(viewLifecycleOwner) { visita ->
            visita?.let { poblarDatos(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            binding.btnIniciarVisita.isEnabled = !isLoading
            binding.btnMarcarNoRealizada.isEnabled = !isLoading

            if (isLoading) binding.errorView.isVisible = false
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            binding.errorView.isVisible = error != null
            if (error != null) binding.errorText.text = error
        }

        viewModel.cancelacionExitosa.observe(viewLifecycleOwner) { exito ->
            if (exito) {
                Toast.makeText(requireContext(), "Visita marcada como NO REALIZADA", Toast.LENGTH_LONG).show()
                parentFragmentManager.popBackStack() // Volver a la lista
            }
        }
    }

    private fun setupListeners() {
        binding.retryButton.setOnClickListener { viewModel.retry() }

        binding.btnIniciarVisita.setOnClickListener {
            if (visitaId != null) {
                val fragment = RegistrarVisitaFragment.newInstance(visitaId!!)
                parentFragmentManager.beginTransaction()
                    .replace((requireView().parent as ViewGroup).id, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.btnMarcarNoRealizada.setOnClickListener {
            NoRealizadaBottomSheet.newInstance().show(parentFragmentManager, NoRealizadaBottomSheet.TAG)
        }
    }

    private fun poblarDatos(visita: VisitaDetalle) {
        binding.textNombreInstitucionHeader.text = visita.nombreInstitucion
        binding.textHoraVisitaHeader.text = formatHora(visita.fechaVisitaProgramada)
        setupFila(binding.rowNombre, "Nombre", visita.nombreInstitucion)
        val dirLimp = limpiarDireccion(visita.direccion)
        setupFila(binding.rowDireccion, "Dirección", dirLimp)
        setupFila(binding.rowContacto, "Contacto", visita.clienteContacto ?: "N/A")
        val productosTexto: String
        if (visita.productosPreferidos?.isNotEmpty() == true) {
            val builder = StringBuilder()
            for (producto in visita.productosPreferidos) {
                // Formato: "• Producto (10 unidades) " (${producto.cantidadTotal} unidades)
                builder.append("• ${producto.nombre} \n")
            }
            productosTexto = builder.trim().toString()
        } else {
            productosTexto = "N/A"
        }
        setupFila(binding.rowProductos, "Sugerencia de productos:", productosTexto)
        setupFila(binding.rowTiempo, "Tiempo de Desplazamiento", "N/A")

        val notasTexto: String
        if (visita.notasVisitasAnteriores?.isNotEmpty() == true) {

            val notasBuilder = StringBuilder()

            for (nota in visita.notasVisitasAnteriores) {
                val fechaFormateada = formatNotaFecha(nota.fechaVisitaProgramada)

                notasBuilder.append("• $fechaFormateada: ")
                notasBuilder.append(nota.detalle ?: "Sin detalle")
                notasBuilder.append("\n") // Salto de línea
            }
            notasTexto = notasBuilder.trim().toString()
        } else {
            notasTexto = visita.detalle ?: "N/A"
        }

        setupFila(binding.rowNotas, "Notas de Visita anterior", notasTexto)

        val esPendiente = visita.estado == "PENDIENTE"
        binding.layoutBotones.isVisible = esPendiente

        val coordenadas = obtenerCoordenadas(visita.direccion)

        if (coordenadas != null) {
            // 1. Hacemos visible el contenedor del mapa
            binding.mapCardView.isVisible = true

            // 2. Obtenemos el fragmento del mapa y cargamos la ubicación
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment

            mapFragment?.getMapAsync { googleMap ->
                // Configuración básica del mapa
                googleMap.uiSettings.isMapToolbarEnabled = false // Quitamos botones de Google por diseño limpio

                // Agregar marcador
                googleMap.addMarker(MarkerOptions().position(coordenadas).title(visita.nombreInstitucion))

                // Mover cámara
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 15f))

                // Tipo de mapa (opcional)
                googleMap.mapType = com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
            }

            // 3. (Opcional) Click en el mapa para abrir Google Maps / Waze
            view?.findViewById<View>(R.id.map_overlay_click)?.setOnClickListener {
                val uri = "geo:${coordenadas.latitude},${coordenadas.longitude}?q=${coordenadas.latitude},${coordenadas.longitude}(${visita.nombreInstitucion})"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                startActivity(intent)
            }

        } else {
            // Si no hay coordenadas o son inválidas, ocultamos el mapa
            binding.mapCardView.isVisible = false
        }
    }

    private fun setupFila(filaBinding: ItemDetalleFilaBinding, label: String, value: String) {
        filaBinding.itemLabel.text = label
        filaBinding.itemValue.text = value
    }

    private fun formatHora(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return "N/A"
        return try {
            val date = isoFormatter.parse(isoDate)
            date?.let { timeFormatter.format(it) } ?: "N/A"
        } catch (e: Exception) { "N/A" }
    }

    private fun formatNotaFecha(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return "Fecha inv."
        return try {
            val date = isoFormatter.parse(isoDate)
            date?.let { notaDateFormatter.format(it) } ?: "Fecha inv."
        } catch (e: Exception) { "Fecha inv." }
    }
}