package com.example.vocab.scanner

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentMethodSelectBinding
import kotlinx.android.synthetic.main.activity_scanner.view.*
import java.util.*

/**
 * Fragment where user choose method of loading words
 */

class MethodSelectFragment : Fragment() {
    private lateinit var binding: FragmentMethodSelectBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMethodSelectBinding.inflate(inflater, container, false)

        val _fragmentModel: ScannerModel by activityViewModels()

        (activity as AppCompatActivity).supportActionBar?.title  = resources.getString(R.string.vocabulary_scanner_choose_method)

        if(Tools.isVertical(resources)){
            binding.layoutMehtod.orientation = LinearLayout.VERTICAL
        }
        else{
            binding.layoutMehtod.orientation = LinearLayout.HORIZONTAL
        }

        binding.unstructuredTextMethod.setOnClickListener {
            _fragmentModel.mode = ScannerModel.scanningMode.unstructuredTxt
            _fragmentModel.process = ScannerModel.scanningProcess.undefined

            binding.root.findNavController().navigate(R.id.toSelectWord)
        }

        binding.structuredTextMethod.setOnClickListener {
            _fragmentModel.mode = ScannerModel.scanningMode.structuredText
            _fragmentModel.process = ScannerModel.scanningProcess.scanningFromWords

            binding.root.findNavController().navigate(R.id.toSelectWord)
        }

        return binding.root
    }
}