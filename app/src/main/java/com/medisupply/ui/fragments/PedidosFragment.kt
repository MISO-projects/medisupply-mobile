package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.data.models.Pedido
import com.medisupply.data.repositories.PedidoRepository
import com.medisupply.data.repositories.network.ApiService
import com.medisupply.data.repositories.network.NetworkServiceAdapter
import com.medisupply.databinding.FragmentPedidosBinding
import com.medisupply.ui.adapters.PedidosAdapter
import com.medisupply.ui.viewmodels.PedidosViewModel
import com.medisupply.ui.viewmodels.PedidosViewModelFactory

class PedidosFragment : Fragment() {

    private var _binding: FragmentPedidosBinding? = null
    private val binding get() = _binding!!

    private lateinit var pedidosAdapter: PedidosAdapter

    private val viewModel: PedidosViewModel by viewModels {
        PedidosViewModelFactory(
            PedidoRepository(
                NetworkServiceAdapter.getInstance().create(ApiService::class.java)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_pedidos,
            container,
            false
        )

        setupUI()
        setupObservers()

        return binding.root
    }

    private fun setupUI() {
        // Configurar RecyclerView
        pedidosAdapter = PedidosAdapter { pedido ->
            onPedidoClick(pedido)
        }

        binding.pedidosRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pedidosAdapter
        }

        // Configurar botón de reintentar
        binding.retryButton.setOnClickListener {
            viewModel.retry()
        }
    }

    private fun setupObservers() {
        // Observar lista de pedidos
        viewModel.pedidos.observe(viewLifecycleOwner) { pedidos ->
            pedidosAdapter.submitList(pedidos)
            updateUIState(pedidos.isEmpty(), false, null)
        }

        // Observar estado de carga
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgressBar.isVisible = isLoading
            if (isLoading) {
                binding.pedidosRecyclerView.isVisible = false
                binding.errorView.isVisible = false
                binding.errorText.isVisible = false
            }
        }

        // Observar errores
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.errorText.text = error
                updateUIState(false, true, error)
            }
        }
    }

    private fun updateUIState(isEmpty: Boolean, hasError: Boolean, error: String? = null) {
        binding.apply {
            pedidosRecyclerView.isVisible = !isEmpty && !hasError
            emptyView.isVisible = isEmpty && !hasError
            errorView.isVisible = hasError
            loadingProgressBar.isVisible = false
        }
    }


    private fun onPedidoClick(pedido: Pedido) {
        // Manejar el clic en el pedido
        Toast.makeText(
            requireContext(),
            "Pedido seleccionado: ${pedido.numeroPedido}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
