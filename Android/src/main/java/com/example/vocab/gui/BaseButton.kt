package com.example.vocab.gui

import android.graphics.drawable.Drawable
import android.view.View
import com.example.vocab.databinding.FragmentVocabularyButtonCellBinding
import com.example.vocab.R

/**
 * Generic button
 *
 * @property itemID used for name of the button
 */
data class BaseButton(private val name: String, private val height: Int, private val layoutShape: Drawable?,
                      private val clickHandler: ()->Unit): BaseItem<FragmentVocabularyButtonCellBinding> {
    override val layoutId: Int = R.layout.fragment_vocabulary_button_cell
    override val itemID: Any = name

    var textSize: Float? = null

    override fun initViewBinding(view: View): FragmentVocabularyButtonCellBinding {
        return FragmentVocabularyButtonCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentVocabularyButtonCellBinding) {
        binding.addListButton.text = name
        binding.addListButton.setOnClickListener{
            clickHandler()
        }

        binding.root.layoutParams.height = height

        layoutShape?.let {
            binding.root.background = layoutShape
        }

        textSize?.let{
            binding.addListButton.textSize = it
        }
    }
}