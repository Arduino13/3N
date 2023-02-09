package com.example.vocab.test

import android.graphics.drawable.Drawable
import android.view.View
import com.example.vocab.databinding.FragmentRankListBinding
import com.example.vocab.gui.BaseItem
import com.example.vocab.R
import com.example.vocab.databinding.FragmentBaseRankCellBinding

/**
 * List cell of rank list
 *
 * @property layout to make round corners
 */
data class BaseRank(val position: Int, val name: String, val values: Pair<Int,Int>): BaseItem<FragmentBaseRankCellBinding> {
    override val layoutId: Int =  R.layout.fragment_base_rank_cell
    override val itemID: Any = name

    var layout: Drawable? = null

    override fun initViewBinding(view: View): FragmentBaseRankCellBinding {
        return FragmentBaseRankCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentBaseRankCellBinding) {
        binding.position.text = position.toString()
        binding.name.text = name
        binding.successRation.text = values.second.toString() + "%"

        layout?.let{
            binding.root.background = layout
        }
    }
}