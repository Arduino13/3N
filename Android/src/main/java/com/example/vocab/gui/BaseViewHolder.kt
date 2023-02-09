package com.example.vocab.gui

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Generic view holder for BaseListAdapter
 */
class BaseViewHolder<T: ViewBinding>(val binding: T) : RecyclerView.ViewHolder(binding.root) {}