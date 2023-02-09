package com.example.vocab.gui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

/**
 * Generic list adapter that is used in all fragments, as view holder is using BaseViewHolder
 */
class BaseListAdapter() :
    ListAdapter<BaseItem<*>, BaseViewHolder<*>>(
        AsyncDifferConfig.Builder(object : DiffUtil.ItemCallback<BaseItem<*>>() {
            override fun areItemsTheSame(oldItem: BaseItem<*>, newItem: BaseItem<*>): Boolean {
                return oldItem.itemID == newItem.itemID
            }

            override fun areContentsTheSame(oldItem: BaseItem<*>, newItem: BaseItem<*>): Boolean {
                return oldItem == newItem
            }
        }).build()
    ) {

    /**
     * Can search item by its layoutId, because BaseListAdapter can contain multiple items
     * with different layouts.
     */
    private fun getItemForViewType(viewType: Int): BaseItem<*> {
        for (i in 0..itemCount) {
            val item: BaseItem<*> = getItem(i)
            if (item.layoutId == viewType) {
                return item
            }
        }
        throw Exception("Could not find model for view type: $viewType")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        val item = getItemForViewType(viewType)

        return BaseViewHolder(
            item.initViewBinding(
                view
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int){
        getItem(position).getBinding(holder)
    }

    override fun getItemViewType(position: Int) = getItem(position).layoutId
}