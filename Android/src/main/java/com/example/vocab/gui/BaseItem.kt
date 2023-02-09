package com.example.vocab.gui
import android.view.View
import androidx.viewbinding.ViewBinding

/**
 * Interface for generic GUI component that can be put inside BaseListAdapter
 *
 * @property layoutId stores id of layout
 * @property itemID unique id
 */
interface BaseItem<T : ViewBinding> {
    val layoutId: Int

    val itemID: Any

    /**
     * Initializes view of component and returns it
     */
    fun initViewBinding(view: View): T

    /**
     * Sets up GUI component, connects callback functions, sets data from model etc.
     */
    fun getBinding(holder: BaseViewHolder<*>) {
        val specificHolder = holder as BaseViewHolder<T>
        getBinding(specificHolder.binding)
    }

    fun getBinding(binding: T)

    override fun equals(other: Any?): Boolean
}