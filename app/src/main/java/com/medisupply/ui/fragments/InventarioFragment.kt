package com.medisupply.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import com.medisupply.R
import com.medisupply.databinding.FragmentInventarioBinding

class InventarioFragment : Fragment() {

    private var _binding: FragmentInventarioBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_inventario,
            container,
            false
        )

        setupUI()

        return binding.root
    }

    private fun setupUI() {
        binding.titleText.text = "Inventario"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
