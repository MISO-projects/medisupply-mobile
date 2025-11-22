package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentDetallePedidoClienteBinding
import com.medisupply.ui.adapters.ProductoDetalleAdapter
import com.medisupply.ui.viewmodels.DetallePedidoClienteViewModel
import com.medisupply.ui.viewmodels.DetallePedidoClienteViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

class DetallePedidoClienteFragment : Fragment() {

    private var _binding: FragmentDetallePedidoClienteBinding? = null
    private val binding get() = _binding!!

    private lateinit var productosAdapter: ProductoDetalleAdapter
    private lateinit var viewModel: DetallePedidoClienteViewModel

    companion object {
        private const val ARG_PEDIDO_ID = "pedido_id"
        private const val ARG_PEDIDO_NUMERO = "pedido_numero"
        private const val ARG_PEDIDO_FECHA = "pedido_fecha"
        private const val ARG_PEDIDO_ESTADO = "pedido_estado"
        private const val ARG_DELIVERY_DATE = "delivery_date"

        fun newInstance(pedido: com.medisupply.data.models.PedidoResumenCliente): DetallePedidoClienteFragment {
            return DetallePedidoClienteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PEDIDO_ID, pedido.id)
                    putString(ARG_PEDIDO_NUMERO, pedido.numeroPedido)
                    putString(ARG_PEDIDO_FECHA, pedido.fechaCreacion)
                    putString(ARG_PEDIDO_ESTADO, pedido.estado)
                    putString(ARG_DELIVERY_DATE, pedido.fechaEntregaEstimada)
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
            R.layout.fragment_detalle_pedido_cliente,
            container,
            false
        )

        setupViewModel()
        setupUI()
        setupObservers()
        loadBasicOrderData()

        return binding.root
    }

    private fun setupViewModel() {
        val pedidoId = arguments?.getString(ARG_PEDIDO_ID) ?: return
        val apiService = NetworkServiceAdapter.getInstance().create(ApiService::class.java)
        val repository = PedidoRepository(apiService)
        val factory = DetallePedidoClienteViewModelFactory(repository, pedidoId)
        viewModel = factory.create(DetallePedidoClienteViewModel::class.java)
    }

    private fun setupUI() {
        productosAdapter = ProductoDetalleAdapter()
        binding.productosRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productosAdapter
        }
    }

    private fun setupObservers() {
        viewModel.pedido.observe(viewLifecycleOwner) { pedido ->
            pedido?.let {
                binding.orderNumber.text = it.numeroPedido
                binding.orderDate.text = formatDate(it.fechaCreacion)
                binding.orderStatus.text = it.estado
                binding.deliveryDate.text = formatDate(it.fechaEntregaEstimada)
                
                binding.shippingAddress.text = it.direccion
                binding.shippingMethod.text = getString(R.string.envio_estandar)

                val productos = it.productos.map { pedidoItem ->
                    ProductoDetalle(
                        nombre = pedidoItem.nombreProducto,
                        cantidad = pedidoItem.cantidad
                    )
                }
                productosAdapter.submitList(productos)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // TODO: Show/hide loading indicator if needed
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadBasicOrderData() {
        arguments?.let { args ->
            val pedidoNumero = args.getString(ARG_PEDIDO_NUMERO)
            val pedidoFecha = args.getString(ARG_PEDIDO_FECHA)
            val pedidoEstado = args.getString(ARG_PEDIDO_ESTADO)
            val deliveryDate = args.getString(ARG_DELIVERY_DATE)

            binding.orderNumber.text = pedidoNumero
            binding.orderDate.text = formatDate(pedidoFecha)
            binding.orderStatus.text = pedidoEstado
            binding.deliveryDate.text = formatDate(deliveryDate)
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        
        return try {
            val inputFormat = SimpleDateFormat(
                if (dateString.contains(" ")) "yyyy-MM-dd HH:mm:ss.SSSSSS" else "yyyy-MM-dd",
                Locale.getDefault()
            )
            
            val outputFormat = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            
            val date = inputFormat.parse(dateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Data class to represent a product in the order detail view
 */
data class ProductoDetalle(
    val nombre: String,
    val cantidad: Int
)

