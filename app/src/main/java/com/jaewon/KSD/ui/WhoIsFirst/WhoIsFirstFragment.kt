package com.jaewon.KSD.ui.WhoIsFirst

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.jaewon.KSD.databinding.FragmentWhoisfirstBinding

class WhoIsFirstFragment : Fragment() {

    private var _binding: FragmentWhoisfirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val whoIsFirstViewModel =
            ViewModelProvider(this).get(WhoIsFirstViewModel::class.java)

        _binding = FragmentWhoisfirstBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textWhoIsFirst
        whoIsFirstViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}