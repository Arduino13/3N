package com.example.vocab.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.R
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentSettingListLanguagesBinding
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.thirdParty.Translator

/**
 * Fragment with list of languages
 */
class SettingListLanguagesFragment : Fragment() {
    private lateinit var binding: FragmentSettingListLanguagesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingListLanguagesBinding.inflate(inflater, container, false)

        val adapterT = BaseListAdapter()
        val view = binding.languagesList

        with(view){
            adapter = adapterT
            layoutManager = LinearLayoutManager(context)
        }

        val listToSubmit = mutableListOf<BaseLanguageCell>()
        for(l in Translator.languages.values()){
            val cell = BaseLanguageCell(resources.getString(l.language), l.language)
            cell.onClick = BaseLanguageCell.OnClickListener{ lan ->
                parentFragmentManager.setFragmentResult("lang_changed", bundleOf(
                    "lang" to lan
                ))
                binding.root.findNavController().popBackStack()
            }

            listToSubmit.add(cell)
        }

        adapterT.submitList(listToSubmit.toList())

        binding.navBar.setOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        return binding.root
    }
}