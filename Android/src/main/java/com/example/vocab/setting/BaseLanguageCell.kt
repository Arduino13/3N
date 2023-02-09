package com.example.vocab.setting

import android.view.View
import com.example.vocab.databinding.FragmentLanguageCellBinding
import com.example.vocab.gui.BaseItem
import com.example.vocab.R

/**
 * Gui component that displays language name in settings
 */
data class BaseLanguageCell(private val name: String, private val stringID: Int): BaseItem<FragmentLanguageCellBinding> {
    class OnClickListener(private val func: (lan: Int)->Unit){
        fun onClick(lan: Int){
            func(lan)
        }
    }

    override val layoutId: Int = R.layout.fragment_language_cell
    override val itemID: Any = stringID

    var onClick: OnClickListener? = null

    override fun initViewBinding(view: View): FragmentLanguageCellBinding {
        return FragmentLanguageCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentLanguageCellBinding) {
        binding.language = name
        binding.stringID = stringID

        onClick?.let{
            binding.clickListener = onClick
        }
    }
}