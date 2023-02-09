package com.example.vocab.vocabulary

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentVocabularyListWordsBinding
import com.example.vocab.gui.BaseHeader
import com.example.vocab.gui.BaseItem

/**
 * Fragment with list of words
 */
abstract class VocabularyListWords(destNewWord: Int): GenericWordList<FragmentVocabularyListWordsBinding>(destNewWord){
    override fun setUpList(list: List<BaseWord>, header: String?): List<BaseItem<*>>{
        val toReturn = mutableListOf<BaseItem<*>>(
            BaseHeader(
                header ?: "null",
                Tools.dp2Pixels(80, resources)
            )
        )

        toReturn.addAll(super.setUpList(list, header))

        return toReturn
    }

    override fun onPopBack() {
        fragmentModel.commit()
    }

    /**
     * Returns used model by child objects
     */
    abstract fun getFragmentModelObj(): GenericVocabularyModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentVocabularyListWordsBinding.inflate(inflater, container, false)

        binding.navBar.title = resources.getString(R.string.vocabulary_list_title)
        initList(binding, binding.listView, binding.navBar, binding.actionButton, getFragmentModelObj())

        return binding.root
    }
}