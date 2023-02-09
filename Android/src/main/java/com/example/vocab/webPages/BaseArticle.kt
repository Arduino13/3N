package com.example.vocab.webPages

import android.view.View
import com.example.vocab.R
import com.example.vocab.databinding.FragmentWebBinding
import com.example.vocab.gui.BaseItem

/**
 * GUI component which displays preview photo of the page, short text from header and article title
 */

data class BaseArticle(val item: RSS.Article, val name: String) : BaseItem<FragmentWebBinding> {
    class TouchListenerClass(private val touchListener: (link: String)->Unit ){
        fun onClick(link: String) = touchListener(link)
    }

    override val layoutId = R.layout.fragment_web
    override val itemID = name

    var touchHandler: TouchListenerClass? = null

    override fun initViewBinding(view: View) = FragmentWebBinding.bind(view)

    override fun getBinding(binding: FragmentWebBinding) {
        binding.picture.setImageBitmap(item.picture)
        binding.title.text = item.header
        binding.contentWeb.text = item.context
        binding.link = item.webLink
        binding.page.text = name
        binding.clickListener = touchHandler
    }
}