package com.example.vocab.vocabulary

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.CheckBox
import androidx.core.view.isVisible
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentVocabularyWordCellBinding
import com.example.vocab.gui.BaseItem

/**
 * Word list cell
 *
 * @property isChecked is used when [editable] is true, for deleting
 * @property editable used when deleting list items, displays checkbox
 * @property checkBoxRef reference to checkBOx
 */
data class BaseWord(val word: Word): BaseItem<FragmentVocabularyWordCellBinding> {
    override val layoutId = R.layout.fragment_vocabulary_word_cell
    override val itemID = Tools.getUUID()

    override fun initViewBinding(view: View): FragmentVocabularyWordCellBinding{
        return FragmentVocabularyWordCellBinding.bind(view)
    }

    private var postEditable = false
    private var postChecked = false

    var isChecked = false
        set(new){
            field=new
            binding.checked.isChecked = new
        }
        get(){
            return binding.checked.isChecked
        }
    var editable = false
        set(new){
            field=new
            binding.checked.isVisible = new
        }

    val checkBoxRef: CheckBox
        get(){
            return binding.checked
        }

    var layout: Drawable? = null
    private lateinit var binding: FragmentVocabularyWordCellBinding

    /**
     * sets [editable] on initialization
     */
    fun postEditable(editable: Boolean){
        postEditable = editable
    }

    /**
     * sets [isChecked] on initialization
     */
    fun postChecked(checked: Boolean){
        postChecked = checked
    }

    override fun getBinding(binding: FragmentVocabularyWordCellBinding) {
        this.binding = binding

        binding.from.text = word.from
        binding.to.text = word.to

        binding.checked.isVisible = postEditable
        binding.checked.isChecked = postChecked

        layout?.let {
            binding.root.background = it
        }
    }
}