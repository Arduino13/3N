package com.example.vocab.vocabulary

import android.os.Bundle
import android.view.*
import android.widget.Toast
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentVocabularyAddListWordsBinding
import com.example.vocab.filters
import com.example.vocab.gui.BaseButtonRight
import com.example.vocab.gui.BaseHeader
import com.example.vocab.gui.BaseInput
import com.example.vocab.gui.BaseItem

/**
 * Fragment for adding new list of words, it's inherited by [StudentVocabularyAddListWords] and
 * [TeacherVocabularyAddListWords]
 */
abstract class VocabularyAddListWords(destNewWord: Int): GenericWordList<FragmentVocabularyAddListWordsBinding>(destNewWord){
    private var inputCellRef: BaseInput? = null
    private var inputCellText: String? = null

    /**
     * Fragment is designed as list of GUI components
     */
    override fun setUpList(list: List<BaseWord>, header: String?): List<BaseItem<*>> {
        val toReturn = mutableListOf<BaseItem<*>>()

        toReturn += BaseHeader(
            resources.getString(R.string.vocabulary_add_list_subtitle)
            , Tools.dp2Pixels(80, resources)
        )

        inputCellRef =
            BaseInput(Tools.dp2Pixels(70, resources))
        toReturn += inputCellRef!!
        if(inputCellRef != null && inputCellText != null){
            inputCellRef!!.textToSet = inputCellText!!
        }

        toReturn += BaseButtonRight(
            resources.getString(R.string.button_base_save2),
            Tools.dp2Pixels(45, resources)
        ) { checkAndPostData() }

        toReturn += BaseHeader(
            resources.getString(R.string.vocabulary_add_list_subtitle2),
            Tools.dp2Pixels(80, resources)
        )

        toReturn.addAll(super.setUpList(list, header))
        return toReturn
    }

    /**
     * Validates and save new list
     */
    private fun checkAndPostData(){
        inputCellRef?.let{
            if(!filters.isValid(it.text)){
                it.error = resources.getString(R.string.wrong_name_error)
            }
            else {
                val listName = it.text
                fragmentModel.words.value?.let {
                    val listOfNewWords: List<BaseItem<*>> = super.setUpList(it, header)
                    if (listOfNewWords.isNotEmpty()) {
                        for (w in listOfNewWords) {
                            val localW = w as BaseWord
                            fragmentModel.save(
                                BaseWord(localW.word.copy(list = listName, newList = false)),
                                accessId
                            )
                            fragmentModel.delete(
                                listOf(localW),
                                accessId
                            )
                        }

                        fragmentModel.commit()
                        super.popBack()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.vocabulary_no_words_error),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onAddWord() {
        fragmentSettingModel.listNameSavedInstance = inputCellRef?.text
    }

    override fun onPopBack() {
        fragmentSettingModel.listNameSavedInstance = null
    }

    /**
     * Returns used model by child objects
     */
    abstract fun getFragmentModelObj(): GenericVocabularyModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentVocabularyAddListWordsBinding.inflate(inflater, container, false)

        binding.navBar.title = resources.getString(R.string.vocabulary_add_list_title)
        initList(binding, binding.listView, binding.navBar, binding.actionButton, getFragmentModelObj())

        inputCellText = fragmentSettingModel.listNameSavedInstance

        return binding.root
    }
}