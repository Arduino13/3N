package com.example.vocab.teacherHome

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.HomeWork
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentTeacherHomeworkAddBinding
import com.example.vocab.filters
import com.example.vocab.globalModel
import com.example.vocab.homework.BaseHomework
import com.example.vocab.teacherVocabulary.TeacherVocabularyModel
import com.example.vocab.vocabulary.VocabularyModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.fragment_teacher_homework_add_words.*
import java.util.*

/**
 * Fragment for adding new homework
 */
class TeacherHomeworkAdd : Fragment() {
    private lateinit var binding: FragmentTeacherHomeworkAddBinding
    private var selectedDate = Date()

    /**
     * For given name of word's list stored in [list], returns list of words within the list
     */
    private fun wordsInList(list: Word): List<Word>{
        val model: globalModel by activityViewModels()
        val factory = VocabularyModelFactory(model)
        val vocabularyFragmentModel = ViewModelProvider(this, factory).get(TeacherVocabularyModel::class.java)

        val toReturn = mutableListOf<Word>()
        for(word in vocabularyFragmentModel.words.value ?: listOf()){
            if(word.word.list == list.list) toReturn.add(word.word)
        }

        return toReturn
    }

    /**
     * Validates and saves data
     */
    private fun closeHandle(fragmentModel: TeacherHomeworkModel){
        if(filters.isValid(binding.homeworkName.text.toString())){
            if(fragmentModel.listOfWordsToAdd.value.isNullOrEmpty() &&
                    fragmentModel.listOfWordsToAdd.value.isNullOrEmpty()){
                Toast.makeText(
                    requireContext(),
                    resources.getText(R.string.homework_add_warning),
                    Toast.LENGTH_LONG).show()

                return
            }

            val wordsToAdd = mutableListOf<Word>()
            wordsToAdd.addAll(fragmentModel.wordsToAdd.value ?: listOf())

            for(list in fragmentModel.listOfWordsToAdd.value ?: listOf()){
                wordsToAdd.addAll(wordsInList(list))
            }

            val homework = BaseHomework(
                HomeWork(
                    Tools.getUUID(),
                    binding.homeworkName.text.toString(),
                    Date(),
                    wordsToAdd,
                    selectedDate,
                    fragmentModel.id.value!!,
                    Tools.getUUID()
                )
            )

            val id = fragmentModel.requestAccess() ?: throw Exception("access denied teacher homework fragment")
            fragmentModel.save(homework, id)
            fragmentModel.commit()
            fragmentModel.releaseAccess()

            binding.root.findNavController().popBackStack()
        }
        else{
            binding.homeworkName.error = resources.getString(R.string.wrong_name_error)
        }
    }

    /**
     * Displays calendar for selecting date
     */
    private fun displayCalendar(fragmentSettingModel: TeacherHomeworkSettingModel){
        val dialog = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.homework_add_calendar_title))
            .build()

        dialog.addOnPositiveButtonClickListener{ selection ->
            binding.homeworkDate.text = dialog.headerText
            selectedDate = Date(selection)

            fragmentSettingModel.dateOfHomework = Date(selection)
            fragmentSettingModel.dateHeaderOfHomework = dialog.headerText
        }
        dialog.show(parentFragmentManager, "tag")
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherHomeworkAddBinding.inflate(inflater, container, false)

        val fragmentModel: TeacherHomeworkModel by activityViewModels()
        val fragmentSettingModel: TeacherHomeworkSettingModel by viewModels()

        fragmentSettingModel.nameOfHomework?.let {
            binding.homeworkName.setText(it)
        }
        fragmentSettingModel.dateOfHomework?.let {
            selectedDate = it
        }
        fragmentSettingModel.dateHeaderOfHomework?.let{
            binding.homeworkDate.text = it
        }

        //sets description for added word's lists
        fragmentModel.listOfWordsToAdd.observe(viewLifecycleOwner, Observer{ listOflists ->
            var listsString = ""
            for((index, data) in listOflists.withIndex()){
                listsString += data.list + if(index != 3 && index != listOflists.size-1) "," else ""

                if(index==3) break
            }

            binding.homeworkListWords.text = (listsString + if(listOflists.size>=4) "..." else "")
        })

        //sets description for added words
        fragmentModel.wordsToAdd.observe(viewLifecycleOwner, Observer { listOfWords ->
            var wordsString = ""
            for((index, data) in listOfWords.withIndex()){
                wordsString += data.from + if(index != 3 && index != listOfWords.size-1) "," else ""

                if(index==3) break
            }

            binding.homeworkWords.text = (wordsString + if(listOfWords.size>=4) "..." else "")
        })

        binding.homeworkDate.setOnClickListener {
            displayCalendar(fragmentSettingModel)
        }

        binding.homeworkListWords.setOnClickListener {
            fragmentSettingModel.nameOfHomework = binding.homeworkName.text.toString()
            binding.root.findNavController().navigate(R.id.toHomeworkChooseWords, bundleOf("list" to true))
        }
        binding.homeworkWords.setOnClickListener {
            fragmentSettingModel.nameOfHomework = binding.homeworkName.text.toString()
            binding.root.findNavController().navigate(R.id.toHomeworkChooseWords, bundleOf("list" to false))
        }

        binding.addHomework.setOnClickListener {
            closeHandle(fragmentModel)
        }

        binding.navBar.setOnClickListener {
            fragmentModel.listOfWordsToAdd.value = listOf()
            fragmentModel.wordsToAdd.value = listOf()
            binding.root.findNavController().popBackStack()
        }

        return binding.root
    }
}