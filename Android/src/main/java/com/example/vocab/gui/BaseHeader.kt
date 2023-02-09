package com.example.vocab.gui

import android.view.Gravity
import android.view.View
import com.example.vocab.databinding.FragmentVocabularyHeaderBinding
import com.example.vocab.gui.BaseItem
import com.example.vocab.R

/**
 * Header in center of the list cell
 *
 * @property itemID used for title of the header
 */
data class BaseHeader(private val title: String, private val height: Int): BaseItem<FragmentVocabularyHeaderBinding> {
    override val layoutId: Int = R.layout.fragment_vocabulary_header
    override val itemID: Any = title

    override fun initViewBinding(view: View): FragmentVocabularyHeaderBinding {
        return FragmentVocabularyHeaderBinding.bind(view)
    }

    override fun getBinding(binding: FragmentVocabularyHeaderBinding) {
        binding.header.text = title
        binding.header.gravity = Gravity.CENTER_VERTICAL

        binding.root.layoutParams.height = height
    }
}