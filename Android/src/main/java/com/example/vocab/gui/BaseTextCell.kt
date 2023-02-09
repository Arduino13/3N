package com.example.vocab.gui

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.View
import com.example.vocab.R
import com.example.vocab.databinding.FragmentBaseTextCellBinding

/**
 * Generic text cell
 *
 * @property alignment alignment from [wordAlign]
 * @property layoutShape used for creating round corners
 */
data class BaseTextCell(val text: String): BaseItem<FragmentBaseTextCellBinding> {
    override val layoutId = R.layout.fragment_base_text_cell
    override val itemID: Any = text

    class wordAlign{
        companion object{
            const val left = 1
            const val center = 2
        }
    }

    var height: Int? = null
    var alignment: Int? = null
    var layoutShape: Drawable? = null

    override fun initViewBinding(view: View): FragmentBaseTextCellBinding {
        return FragmentBaseTextCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentBaseTextCellBinding){
        binding.textView.text = text

        alignment?.let {
            binding.textView.gravity = if(alignment == BaseTextCell.wordAlign.center) Gravity.CENTER else Gravity.CENTER_VERTICAL
        }

        height?.let {
            binding.root.layoutParams.height = it
        }

        layoutShape?.let {
            binding.root.background = it
        }
    }
}