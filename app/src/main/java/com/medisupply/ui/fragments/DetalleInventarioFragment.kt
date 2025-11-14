package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.medisupply.R
import com.medisupply.data.models.Inventario
import com.medisupply.databinding.FragmentDetalleInventarioBinding

class DetalleInventarioFragment : Fragment() {

    private var _binding: FragmentDetalleInventarioBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_INVENTARIO = "inventario"

        fun newInstance(inventario: Inventario): DetalleInventarioFragment {
            return DetalleInventarioFragment().apply {
                arguments = Bundle().apply {
                    @Suppress("DEPRECATION")
                    putSerializable(ARG_INVENTARIO, inventario)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_detalle_inventario,
            container,
            false
        )

        setupUI()
        loadInventarioData()

        return binding.root
    }

    private fun setupUI() {
    }

    private fun loadInventarioData() {
        @Suppress("DEPRECATION")
        val inventario = arguments?.getSerializable(ARG_INVENTARIO) as? Inventario

        inventario?.let {
            Glide.with(requireContext())
                .load(it.productoImagenUrl)
                .placeholder(R.drawable.ic_hospital_placeholder)
                .error(R.drawable.ic_hospital_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.productImage)

            binding.productName.text = it.productoNombre
            binding.productSku.text = it.productoSku
            binding.category.text = it.categoria
            binding.quantity.text = it.cantidad.toString()
            binding.unitMeasure.text = it.productoUnidadMedida
            binding.unitPrice.text = it.productoPrecioUnitario
            binding.status.text = it.estado
            binding.lot.text = it.lote
            binding.location.text = it.ubicacion
            binding.storageType.text = it.productoTipoAlmacenamiento
            binding.temperature.text = it.temperaturaRequerida
            binding.expirationDate.text = it.fechaVencimiento
            binding.description.text = it.productoDescripcion
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

