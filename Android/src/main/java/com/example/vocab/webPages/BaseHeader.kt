package com.example.vocab.webPages

import android.view.View
import com.example.vocab.R
import com.example.vocab.databinding.FragmentWebHeaderBinding
import com.example.vocab.gui.BaseItem

data class BaseHeader(val name: String) : BaseItem<FragmentWebHeaderBinding> {

    override val layoutId = R.layout.fragment_web_header

    override val itemID = name

    override fun initViewBinding(view: View) = FragmentWebHeaderBinding.bind(view)

    override fun getBinding(binding: FragmentWebHeaderBinding) {
        binding.header.text = name
    }
}