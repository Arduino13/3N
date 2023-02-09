package com.example.vocab.gui

import android.view.View
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.SpacerCellBinding

/**
 * Currently unused
 */
data class BaseSpacer(private val height: Int):BaseItem<SpacerCellBinding> {
    override val layoutId = R.layout.spacer_cell
    override val itemID = Tools.getUUID()

    override fun initViewBinding(view: View): SpacerCellBinding {
        return SpacerCellBinding.bind(view)
    }

    override fun getBinding(binding: SpacerCellBinding) {
        binding.root.layoutParams.height = height
    }
}