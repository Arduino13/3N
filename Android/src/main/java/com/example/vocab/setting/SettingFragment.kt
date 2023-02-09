package com.example.vocab.setting

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentSettingBinding
import com.example.vocab.thirdParty.Translator
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var fragmentModel: SettingModel
    private lateinit var languageSelected: Translator.languages

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        val factory = SettingFactoryModel(
            Database(requireContext(), requireActivity())
        )
        fragmentModel = ViewModelProvider(this, factory).get(SettingModel::class.java)

        binding.nightMode.isChecked = fragmentModel.nightMode
        binding.rssMaxNum.setText(fragmentModel.maxNumRss.toString())
        binding.language.text = resources.getString(fragmentModel.language.language)
        binding.AppLanguage.text = resources.getString(fragmentModel.AppLanguage.language)
        languageSelected = fragmentModel.language
        binding.wordMaxNum.setText(fragmentModel.maxNumOfWords.toString())
        binding.reverseTranslate.isChecked = fragmentModel.reverseWords

        binding.nightMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(
                if(binding.nightMode.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.language.setOnClickListener {
            parentFragmentManager.setFragmentResultListener("lang_changed", requireActivity(),
                FragmentResultListener{ _, data ->
                    val factory = SettingFactoryModel(
                        Database(requireContext(), requireActivity())
                    )
                    val fragmentModel = ViewModelProvider(this, factory).get(SettingModel::class.java)
                    fragmentModel.language = Translator.fromID(
                        data.getInt("lang")
                    ) ?: Translator.languages.en
                }
            )
            binding.root.findNavController().navigate(R.id.toLanguageList)
        }

        binding.AppLanguage.setOnClickListener {
            parentFragmentManager.setFragmentResultListener("lang_changed", requireActivity(),
                FragmentResultListener{ _, data ->
                    val factory = SettingFactoryModel(
                        Database(requireContext(), requireActivity())
                    )
                    val fragmentModel = ViewModelProvider(this, factory).get(SettingModel::class.java)
                    fragmentModel.AppLanguage = Translator.fromID(
                        data.getInt("lang")
                    ) ?: Translator.languages.en

                    Tools.setLocale(requireActivity(), Translator.lang2code(fragmentModel.AppLanguage))
                }
            )
            binding.root.findNavController().navigate(R.id.toLanguageList)
        }

        binding.logOut.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.setting_log_out_title))
                .setMessage(resources.getString(R.string.setting_log_out_question))
                .setNegativeButton(resources.getString(R.string.button_base_negative_no)) { _,_ -> }
                .setPositiveButton(resources.getString(R.string.button_base_positive)) { _,_ ->
                    Database(requireContext(), requireActivity()).logout()
                    startActivity(Intent.makeRestartActivityTask(activity?.intent?.component))
                }.show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentModel.nightMode = binding.nightMode.isChecked
        fragmentModel.maxNumRss = binding.rssMaxNum.text.toString().toInt()
        fragmentModel.language = languageSelected
        fragmentModel.maxNumOfWords = binding.wordMaxNum.text.toString().toInt()
        fragmentModel.reverseWords = binding.reverseTranslate.isChecked

        super.onDestroyView()
    }
}