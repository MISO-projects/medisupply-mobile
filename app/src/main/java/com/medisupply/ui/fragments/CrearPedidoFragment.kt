package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.medisupply.R
import com.medisupply.databinding.FragmentCrearPedidoBinding

class CrearPedidoFragment : Fragment() {

    private var _binding: FragmentCrearPedidoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_crear_pedido,
            container,
            false
        )

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        // Setup institution dropdown
        setupInstitutionDropdown()

        // Setup products RecyclerView
        setupProductsList()

        // Setup create order button
        binding.btnCrearPedido.setOnClickListener {
            createOrder()
        }

        // Setup search functionality
        setupSearch()
    }

    private fun setupInstitutionDropdown() {
        // TODO: Replace with actual data from ViewModel
        val institutions = listOf(
            "Hospital General",
            "Clínica Santa María",
            "Centro de Salud Norte",
            "Hospital Universitario"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            institutions
        )

        binding.institutionAutocomplete.setAdapter(adapter)
    }

    private fun setupProductsList() {
        // TODO: Setup RecyclerView with products adapter
        binding.productsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            // adapter = productsAdapter
        }
    }

    private fun setupSearch() {
        // TODO: Implement search functionality
        binding.searchProductoText.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchProductoText.text.toString()
            // Filter products
            false
        }
    }

    private fun createOrder() {
        val institution = binding.institutionAutocomplete.text.toString()
        val notes = binding.notesEditText.text.toString()

        if (institution.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Por favor seleccione una institución",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // TODO: Validate products and create order
        Toast.makeText(
            requireContext(),
            "Creando pedido...",
            Toast.LENGTH_SHORT
        ).show()

        // After successful creation, navigate back
        parentFragmentManager.popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
