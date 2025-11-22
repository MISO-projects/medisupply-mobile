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
import com.medisupply.data.models.Cliente
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentResumenPedidoBinding
import com.medisupply.ui.adapters.ProductoConCantidad
import com.medisupply.ui.adapters.ProductoResumenAdapter
import com.medisupply.ui.viewmodels.ResumenPedidoViewModel
import com.medisupply.ui.viewmodels.ResumenPedidoViewModelFactory
import java.text.DecimalFormat

class ResumenPedidoFragment : Fragment() {

    private var _binding: FragmentResumenPedidoBinding? = null
    private val binding get() = _binding!!

    private lateinit var productosAdapter: ProductoResumenAdapter
    private val decimalFormat = DecimalFormat("#,##0.00")

    private val viewModel: ResumenPedidoViewModel by viewModels {
        val apiService = NetworkServiceAdapter.getInstance().create(ApiService::class.java)
        ResumenPedidoViewModelFactory(
            PedidoRepository(apiService)
        )
    }

    companion object {
        private const val ARG_CLIENTE = "cliente"
        private const val ARG_PRODUCTOS = "productos"
        private const val ARG_OBSERVACIONES = "observaciones"
        private const val ARG_VENDEDOR_ID = "vendedor_id"

        fun newInstance(
            cliente: Cliente,
            productos: ArrayList<ProductoConCantidad>,
            observaciones: String?,
            vendedorId: String,
        ): ResumenPedidoFragment {
            return ResumenPedidoFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CLIENTE, cliente)
                    putSerializable(ARG_PRODUCTOS, productos)
                    putString(ARG_OBSERVACIONES, observaciones)
                    putString(ARG_VENDEDOR_ID, vendedorId)
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
            R.layout.fragment_resumen_pedido,
            container,
            false
        )

        setupUI()
        loadArguments()
        setupObservers()

        return binding.root
    }

    private fun setupUI() {
        // Setup productos RecyclerView
        productosAdapter = ProductoResumenAdapter()
        binding.productosResumenPedidoRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productosAdapter
        }

        // Setup buttons
        binding.btnConfirmarPedido.setOnClickListener {
            confirmarPedido()
        }

        binding.btnEditarPedido.setOnClickListener {
            editarPedido()
        }

        // Listen to notes changes
        binding.notesEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val notes = binding.notesEditText.text?.toString()
                viewModel.updateObservaciones(notes)
            }
        }
    }

    private fun loadArguments() {
        arguments?.let { args ->
            @Suppress("DEPRECATION")
            val cliente = args.getSerializable(ARG_CLIENTE) as? Cliente
            @Suppress("DEPRECATION")
            val productos = args.getSerializable(ARG_PRODUCTOS) as? ArrayList<ProductoConCantidad>
            val observaciones = args.getString(ARG_OBSERVACIONES)
            val vendedorId = args.getString(ARG_VENDEDOR_ID) ?: ""

            if (cliente != null && productos != null) {
                viewModel.setOrderData(cliente, productos, observaciones, vendedorId)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_datos_pedido_no_validos),
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun setupObservers() {
        viewModel.cliente.observe(viewLifecycleOwner) { cliente ->
            binding.clienteNombre.text = cliente.nombre
        }

        viewModel.productos.observe(viewLifecycleOwner) { productos ->
            productosAdapter.submitList(productos)
        }

        viewModel.observaciones.observe(viewLifecycleOwner) { observaciones ->
            if (binding.notesEditText.text?.toString() != observaciones) {
                binding.notesEditText.setText(observaciones)
            }
        }

        viewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.subtotalValue.text = getString(R.string.precio_formato, decimalFormat.format(subtotal))
        }

        viewModel.impuestos.observe(viewLifecycleOwner) { impuestos ->
            binding.impuestosValue.text = getString(R.string.precio_formato, decimalFormat.format(impuestos))
        }

        viewModel.total.observe(viewLifecycleOwner) { total ->
            binding.totalValue.text = getString(R.string.precio_formato, decimalFormat.format(total))
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            binding.btnConfirmarPedido.isEnabled = !isLoading
            binding.btnEditarPedido.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }

        viewModel.pedidoCreado.observe(viewLifecycleOwner) { response ->
            if (response != null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.pedido_creado_exitosamente, response.numeroPedido),
                    Toast.LENGTH_LONG
                ).show()
                // Navigate back to orders list (pop twice to remove both fragments)
                parentFragmentManager.popBackStack()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun confirmarPedido() {
        // Update observaciones before confirming
        val notes = binding.notesEditText.text?.toString()
        viewModel.updateObservaciones(notes)
        
        // Confirm order
        viewModel.confirmarPedido()
    }

    private fun editarPedido() {
        // Go back to edit the order
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}