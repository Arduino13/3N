package com.example.vocab.teacherHome

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentTeacherHomeworkAddWordsBinding
import com.example.vocab.globalModel
import com.example.vocab.gui.*
import com.example.vocab.teacherVocabulary.TeacherVocabularyModel
import com.example.vocab.vocabulary.BaseWord
import com.example.vocab.vocabulary.VocabularyModelFactory

/**
 * Fragment for adding word's lists and words
 */
class TeacherHomeworkAddWords : Fragment() {
    private lateinit var binding: FragmentTeacherHomeworkAddWordsBinding

    /**
     * @return shape for list item with 'random' color
     */
    private fun getShape(w: BaseList): Drawable?{
        val db = Database(requireContext(), requireActivity())

        val shape = ContextCompat.getDrawable(requireContext(), R.drawable.layout_circle)
        val shapeColor = (db.getSetting(w.list.list) as? Int) ?: run {
            val generatedColor = Colors.generateColor(resources)!!
            db.saveSetting(
                mapOf<String,Int>(
                    w.list.list to generatedColor
                )
            )
            generatedColor
        }
        shape?.colorFilter = PorterDuffColorFilter(shapeColor, PorterDuff.Mode.OVERLAY)

        return shape
    }

    /**
     * Used in [setUpList] to find if checkbox should be checked
     */
    private fun isListChecked(list: Word, fragmentModel: TeacherHomeworkModel): Boolean{
        for(l in fragmentModel.listOfWordsToAdd.value ?: listOf()){
            if(l.list == list.list) return true
        }

        return false
    }

    /**
     * Find which words should be checked as selected because it's list is selected
     */
    private fun wordsInList(list: Word, vocabularyModel: TeacherVocabularyModel): List<Word>{
        val toReturn = mutableListOf<Word>()
        for(word in vocabularyModel.words.value ?: listOf()){
            if(word.word.list == list.list) toReturn.add(word.word)
        }

        return toReturn
    }

    /**
     * Checks if [word] is presented in [list]
     */
    private fun containWord(word: BaseWord, list: List<Word>): Boolean{
        for(w in list){
            if(w.id == word.word.id) return true
        }

        return false
    }

    /**
     * Removes from word list words that were selected because their list was selected, therefore
     * they can't be displayed in section for choosing words, otherwise user could remove already
     * selected words
     *
     * @return filtered word list for section for choosing words to add
     */
    private fun cleanWordList(list: List<BaseWord>, fragmentModel: TeacherHomeworkModel, vocabularyModel: TeacherVocabularyModel): List<BaseWord>{
        val listToSubmit = mutableListOf<BaseWord>()
        listToSubmit.addAll(list)

        for(l in fragmentModel.listOfWordsToAdd.value ?: listOf()){
            val wordsInList = wordsInList(l, vocabularyModel)
            for(word in list){
                if(containWord(word, wordsInList)){
                    listToSubmit.remove(word)
                }
            }
        }

        return listToSubmit
    }

    /**
     * Sets up list base on if items are list's names or words [isList]
     */
    private fun setUpList(isList: Boolean, fragmentModel: TeacherHomeworkModel, vocabularyModel: TeacherVocabularyModel): List<BaseItem<*>>{
        val listToReturn = mutableListOf<BaseItem<*>>()

        if(isList){
            for(list in vocabularyModel.lists.value ?: listOf()){
                val baseList = BaseList(
                    Word(
                        id = Tools.getUUID(),
                        from = "",
                        to = "",
                        list = list.text
                    )
                )
                baseList.shape = getShape(baseList)

                baseList.postChecked(isListChecked(baseList.list, fragmentModel))

                listToReturn.add(baseList)
            }
        }
        else{
            for(word in cleanWordList(vocabularyModel.words.value ?: listOf(), fragmentModel, vocabularyModel)){
                word.postEditable(true)
                word.layout = Tools.getCircleLayout(R.color.gray, requireContext(), resources)

                if(word.word in fragmentModel.wordsToAdd.value ?: listOf()){
                    word.postChecked(true)
                }

                listToReturn.add(word)
            }
        }

        return listToReturn
    }

    /**
     * Validates and saves data
     * [isList] specifies if items are list's names or words
     */
    private fun closeHandle(fragmentModel: TeacherHomeworkModel, isList: Boolean){
        val toSetList = mutableListOf<Word>()
        val adapter = binding.listOfWordsToAdd.adapter as BaseListAdapter
        val list = adapter.currentList

        if(isList){
            for(l in list){
                (l as? BaseList)?.let{
                    if(it.isChecked) toSetList.add(l.list)
                }
            }
        }
        else{
            for(w in list){
                (w as? BaseWord)?.let{
                    if(it.isChecked) toSetList.add(w.word)
                }
            }
        }

        if(isList){
            fragmentModel.listOfWordsToAdd.postValue(toSetList)
        }
        else{
            fragmentModel.wordsToAdd.postValue(toSetList)
        }

        binding.root.findNavController().popBackStack()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherHomeworkAddWordsBinding.inflate(inflater, container, false)

        val fragmentModel: TeacherHomeworkModel by activityViewModels()
        val isList = arguments?.getBoolean("list") ?: throw Exception("internal error - null list")

        val model: globalModel by activityViewModels()
        val factory = VocabularyModelFactory(model)
        val vocabularyFragmentModel = ViewModelProvider(this, factory).get(TeacherVocabularyModel::class.java)

        if(isList){
            binding.navBar.setTitle(R.string.homework_add_choose_list)
        }
        else{
            binding.navBar.setTitle(R.string.homework_add_choose_word)
        }

        val adapterT = BaseListAdapter()
        with(binding.listOfWordsToAdd){
            layoutManager = LinearLayoutManager(context)
            adapter = adapterT
            addItemDecoration(LinearSpacingDecoration(15,20))
        }

        adapterT.submitList(setUpList(isList, fragmentModel, vocabularyFragmentModel))

        binding.navBar.setOnClickListener {
            closeHandle(fragmentModel, isList)
        }

        return binding.root
    }
}