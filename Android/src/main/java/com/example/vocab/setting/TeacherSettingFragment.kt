package com.example.vocab.setting

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.vocab.MainActivity
import com.example.vocab.R
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentTeacherSettingBinding
import com.example.vocab.thirdParty.Translator
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Settings tab for teacher's part of application
 */
class TeacherSettingFragment: Fragment(){
    private lateinit var binding: FragmentTeacherSettingBinding
    private lateinit var fragmentModel: SettingModel
    private lateinit var languageSelected: Translator.languages

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherSettingBinding.inflate(inflater, container, false)

        val factory = SettingFactoryModel(
            Database(requireContext(), requireActivity())
        )
        fragmentModel = ViewModelProvider(this, factory).get(SettingModel::class.java)

        binding.nightModeTeacher.isChecked = fragmentModel.nightMode
        binding.languageTeacher.text = resources.getString(fragmentModel.language.language)
        binding.AppLanguage.text = resources.getString(fragmentModel.AppLanguage.language)
        languageSelected = fragmentModel.language

        binding.nightModeTeacher.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(
                if(binding.nightModeTeacher.isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.languageTeacher.setOnClickListener {
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
                }
            )
            binding.root.findNavController().navigate(R.id.toLanguageList)
        }

        binding.logOutTeacher.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.setting_log_out_title))
                .setMessage(resources.getString(R.string.setting_log_out_question))
                .setNegativeButton(resources.getString(R.string.button_base_negative_no)) { _,_ -> }
                .setPositiveButton(resources.getString(R.string.button_base_positive)) { _,_ ->
                    Database(requireContext(), requireActivity()).logout()
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)

                    requireActivity().finish()
                }.show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        fragmentModel.nightMode = binding.nightModeTeacher.isChecked
        fragmentModel.language = languageSelected

        super.onDestroyView()
    }
}