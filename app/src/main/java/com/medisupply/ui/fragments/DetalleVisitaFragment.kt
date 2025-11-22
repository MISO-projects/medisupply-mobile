package com.medisupply.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.location.LocationServices
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
        DetalleVisitaViewModelFactory(requireActivity().application, repository, visitaId!!)
    }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            recargarDetalleConUbicacion()
        } else {
            recargarDetalleSinUbicacion()
        }
    }

    private val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val notaDateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    companion object {
        private const val ARG_VISITA_ID = "visita_id"
        fun newInstance(id: String) = DetalleVisitaFragment().apply {
            arguments = Bundle().apply { putString(ARG_VISITA_ID, id) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { visitaId = it.getString(ARG_VISITA_ID) }

        setFragmentResultListener("request_cancelar_visita") { _, b ->
            viewModel.cancelarVisita(b.getString("motivo") ?: "")
        }
        setFragmentResultListener("request_refresh") { _, b ->
            if (b.getBoolean("should_refresh")) parentFragmentManager.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detalle_visita, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (visitaId == null) { parentFragmentManager.popBackStack(); return }
        setupToolbar()
        setupObservers()
        setupListeners()
        pedirPermisoYRecargarDetalle()
    }

    private fun setupToolbar() { binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() } }

    private fun setupObservers() {
        viewModel.visita.observe(viewLifecycleOwner) { if (it != null) poblarDatos(it) }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.loadingProgressBar.isVisible = it
            binding.btnIniciarVisita.isEnabled = !it
            if (it) binding.errorView.isVisible = false
        }
        viewModel.error.observe(viewLifecycleOwner) {
            binding.errorView.isVisible = it != null
            if (it != null) binding.errorText.text = it
        }
        viewModel.cancelacionExitosa.observe(viewLifecycleOwner) { if (it) parentFragmentManager.popBackStack() }
    }

    private fun setupListeners() {
        binding.retryButton.setOnClickListener { pedirPermisoYRecargarDetalle() }
        binding.btnIniciarVisita.setOnClickListener {
            val f = RegistrarVisitaFragment.newInstance(visitaId!!)
            parentFragmentManager.beginTransaction()
                .replace((requireView().parent as ViewGroup).id, f)
                .addToBackStack(null)
                .commit()
        }
        binding.btnMarcarNoRealizada.setOnClickListener {
            NoRealizadaBottomSheet.newInstance().show(parentFragmentManager, "TAG")
        }
    }

    private fun poblarDatos(visita: VisitaDetalle) {
        binding.textNombreInstitucionHeader.text = visita.nombreInstitucion
        val tiempoOFecha = if (visita.estado == "PENDIENTE" && !visita.tiempoDesplazamiento.isNullOrEmpty())
            getString(R.string.aproximadamente_tiempo, visita.tiempoDesplazamiento) else formatNotaFecha(visita.fechaVisitaProgramada)
        binding.textHoraVisitaHeader.text = tiempoOFecha

        setupFila(binding.rowNombre, getString(R.string.nombre), visita.nombreInstitucion ?: getString(R.string.na))
        setupFila(binding.rowDireccion, getString(R.string.direccion), limpiarDireccion(visita.direccion))
        setupFila(binding.rowContacto, getString(R.string.contacto), visita.clienteContacto ?: getString(R.string.na))

        val prod = if (!visita.productosPreferidos.isNullOrEmpty())
            visita.productosPreferidos.joinToString("\n") { "• ${it.nombre}" } else getString(R.string.na)
        setupFila(binding.rowProductos, getString(R.string.sugerencia), prod)

        setupFila(binding.rowTiempo, getString(R.string.tiempo_desplazamiento), visita.tiempoDesplazamiento ?: getString(R.string.na))

        val notas = if (!visita.notasVisitasAnteriores.isNullOrEmpty())
            visita.notasVisitasAnteriores.joinToString("\n") { "• ${formatNotaFecha(it.fechaVisitaProgramada)}: ${it.detalle}" }
        else visita.detalle ?: getString(R.string.na)
        setupFila(binding.rowNotas, getString(R.string.notas_detalle), notas)

        // --- LÓGICA DE EVIDENCIA (FOTO vs VIDEO) ---
        val url = visita.evidencia
        if (!url.isNullOrEmpty()) {
            binding.evidenceTitle.isVisible = true
            binding.evidenceCard.isVisible = true

            val extension = url.substringAfterLast('.', "").lowercase()
            val esVideo = extension in listOf("mp4", "mov", "avi", "3gp", "mkv")

            if (esVideo) {
                // MODO VIDEO
                binding.imageEvidence.isVisible = false
                binding.videoEvidence.isVisible = true
                binding.iconPlayOverlay.isVisible = true 
                val mediaController = MediaController(requireContext())
                mediaController.setAnchorView(binding.videoEvidence)
                binding.videoEvidence.setMediaController(mediaController)
                binding.videoEvidence.setVideoURI(Uri.parse(url))

                // Listener para el overlay de play
                binding.iconPlayOverlay.setOnClickListener {
                    binding.iconPlayOverlay.isVisible = false
                    binding.videoEvidence.start()
                }
                // Si el video termina, mostramos el play de nuevo
                binding.videoEvidence.setOnCompletionListener {
                    binding.iconPlayOverlay.isVisible = true
                }

            } else {
                // MODO FOTO
                binding.videoEvidence.isVisible = false
                binding.iconPlayOverlay.isVisible = false
                binding.imageEvidence.isVisible = true

                Glide.with(this)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.imageEvidence)
            }
        } else {
            // SIN EVIDENCIA (Ocultar todo)
            binding.evidenceTitle.isVisible = false
            binding.evidenceCard.isVisible = false
        }
        setupFila(binding.rowProductos, getString(R.string.recomendacion), visita.recomendacionLlm ?: getString(R.string.no_disponible))

        // Usar el tiempo de desplazamiento de la API
        setupFila(binding.rowTiempo, getString(R.string.tiempo_desplazamiento_completo), visita.tiempoDesplazamiento ?: getString(R.string.na))

        val notasTexto: String
        if (visita.notasVisitasAnteriores?.isNotEmpty() == true) {
            val notasBuilder = StringBuilder()
            for (nota in visita.notasVisitasAnteriores) {
                val fechaFormateada = formatNotaFecha(nota.fechaVisitaProgramada)
                notasBuilder.append("• $fechaFormateada: ")
                notasBuilder.append(nota.detalle ?: getString(R.string.sin_detalle))
                notasBuilder.append("\n")
            }
            notasTexto = notasBuilder.trim().toString()
        } else {
            notasTexto = visita.detalle ?: getString(R.string.na)
        }

        setupFila(binding.rowNotas, getString(R.string.notas_visita_anterior), notasTexto)

        val esPendiente = visita.estado == "PENDIENTE"
        binding.layoutBotones.isVisible = esPendiente
        setupMap(visita)
    }

    private fun setupFila(b: ItemDetalleFilaBinding, l: String, v: String) {
        b.itemLabel.text = l
        b.itemValue.text = v
    }

    private fun setupMap(visita: VisitaDetalle) {
        val coords = obtenerCoordenadas(visita.direccion)
        binding.mapCardView.isVisible = coords != null
        if (coords != null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap ->
                googleMap.uiSettings.isMapToolbarEnabled = false
                googleMap.addMarker(MarkerOptions().position(coords).title(visita.nombreInstitucion))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coords, 15f))
            }
            binding.root.findViewById<View>(R.id.map_overlay_click)?.setOnClickListener {
                val uri = "geo:${coords.latitude},${coords.longitude}?q=${coords.latitude},${coords.longitude}"
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
            }
        }
    }

    private fun obtenerCoordenadas(d: String?): LatLng? {
        if (d.isNullOrEmpty()) return null
        return try { val p = d.split(","); LatLng(p[0].toDouble(), p[1].toDouble()) } catch (e: Exception) { null }
    }

    private fun limpiarDireccion(d: String?): String =
        if (!d.isNullOrEmpty() && d.split(",").size > 2) d.split(",").subList(2, d.split(",").size).joinToString(",") else d ?: getString(R.string.na)

    private fun formatNotaFecha(d: String?): String =
        try { notaDateFormatter.format(isoFormatter.parse(d)!!) } catch (e: Exception) { getString(R.string.fecha_invalida) }

    private fun pedirPermisoYRecargarDetalle() = recargarDetalleSinUbicacion()

    @SuppressLint("MissingPermission")
    private fun recargarDetalleConUbicacion() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) viewModel.loadVisitaDetalle(it.latitude, it.longitude)
            else recargarDetalleSinUbicacion()
        }.addOnFailureListener { recargarDetalleSinUbicacion() }
    }

    private fun recargarDetalleSinUbicacion() {
        viewModel.loadVisitaDetalle(7.1384581600911945, -73.12422778151247)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}