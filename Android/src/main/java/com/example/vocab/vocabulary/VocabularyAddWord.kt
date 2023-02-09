package com.example.vocab.vocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentVocabularyAddWordBinding
import com.example.vocab.filters
import com.example.vocab.thirdParty.Translator
import java.lang.Exception

/**
 * Fragment for adding new word, fragment is intherited by [StudentVocabularyAddWord] and
 * [TeacherVocabularyAddWord]
 */
abstract class VocabularyAddWord: Fragment() {
    private lateinit var binding: FragmentVocabularyAddWordBinding
    private lateinit var fragmentModel: GenericVocabularyModel
    private lateinit var accessId: String
    private var list: String? = null

    private fun checkAndPostData(){
        val from = binding.fromWord.text.toString()
        val to = binding.toWord.text.toString()
        var correct = true

        val wordToSave = BaseWord(Word(Tools.getUUID(), from, to, class_id = fragmentModel.id.value!!,
            list = if(list!=null) list!! else "", newList = list==null))

        if(!filters.isValid(from)){
            binding.fromWord.error = resources.getString(R.string.wrong_name_error)
            correct = false
        }

        if(!filters.isValid(to)){
            binding.toWord.error = resources.getString(R.string.wrong_name_error)
            correct = false
        }

        for(word in fragmentModel.words.value ?: listOf()){
            if(word.word.from == wordToSave.word.from &&
                !wordToSave.word.newList &&
                word.word.list == wordToSave.word.list) {
                binding.fromWord.error = resources.getString(R.string.vocabulary_word_already_exist)
                correct = false
            }
        }

        if(correct){
            fragmentModel.save(
                wordToSave,
                accessId
            )
            binding.root.findNavController().popBackStack()
        }
    }

    /**
     * Returns used model by child objects
     */
    abstract  fun getFragmentModelObj(): GenericVocabularyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVocabularyAddWordBinding.inflate(inflater, container, false)

        list = arguments?.getString("list")
        accessId = arguments?.getString("accessId") ?: throw Exception("Access to fragment view model denied")

        fragmentModel = getFragmentModelObj()

        binding.navBar.setOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        binding.addNewWord.setOnClickListener {
            checkAndPostData()
        }

        binding.translateAction.setOnClickListener {
            Translator.translate(
                Translator.fromID((Database(requireContext(), requireActivity()).getSetting(Settings.language) as? Int)
                    ?: Settings.languageDef.language)
                    ?: Settings.languageDef,
                Translator.fromID((Database(requireContext(), requireActivity()).getSetting(Settings.AppLanguage) as? Int)
                    ?: Settings.languageDef.language)
                    ?: Settings.languageDef,
                binding.fromWord.text.toString()
            ){ translation ->
                binding.toWord.setText(translation)
            }
        }

        return binding.root
    }
}