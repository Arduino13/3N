package com.example.vocab.gui

import android.view.View
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentVocabularyInputCellBinding

/**
 * Input text box
 *
 * @property textToSet to display predefined text
 * @property text current value of text box
 * @property error string representation of error
 */
data class BaseInput(private val height: Int): BaseItem<FragmentVocabularyInputCellBinding> {
    override val layoutId: Int = com.example.vocab.R.layout.fragment_vocabulary_input_cell
    override val itemID: Any = Tools.getUUID()

    var textToSet: String? = null

    private lateinit var binding: FragmentVocabularyInputCellBinding

    var text: String
        get(){
            return binding.inputList.text.toString()
        }
        set(new){
            binding.inputList.setText(new)
        }

    var error: String
        get(){
            return binding.inputList.error.toString()
        }
        set(new){
            binding.inputList.error = new
        }

    override fun initViewBinding(view: View): FragmentVocabularyInputCellBinding {
        return FragmentVocabularyInputCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentVocabularyInputCellBinding) {
        this.binding = binding

        binding.root.layoutParams.height = height
        binding.inputList.setText(textToSet)
    }
}