package com.example.vocab.gui

import android.graphics.drawable.Drawable
import android.view.View
import com.example.vocab.R
import com.example.vocab.databinding.FragmentVocabularyButtonCellBinding
import com.example.vocab.databinding.FragmentVocabularyButtonCellRightBinding
import com.example.vocab.gui.BaseItem

/**
 * Generic button which is on the right of the list cell
 *
 * @property itemID used for name of the button
 */
data class BaseButtonRight(private val name: String, private val height: Int, private val clickHandler: ()->Unit):
    BaseItem<FragmentVocabularyButtonCellRightBinding> {
    override val layoutId: Int = R.layout.fragment_vocabulary_button_cell_right
    override val itemID: Any = name

    override fun initViewBinding(view: View): FragmentVocabularyButtonCellRightBinding {
        return FragmentVocabularyButtonCellRightBinding.bind(view)
    }

    override fun getBinding(binding: FragmentVocabularyButtonCellRightBinding) {
        binding.addListButton.text = name
        binding.addListButton.setOnClickListener{
            clickHandler()
        }

        binding.root.layoutParams.height = height
    }
}