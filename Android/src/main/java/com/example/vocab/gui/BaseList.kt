package com.example.vocab.gui

import android.graphics.drawable.Drawable
import android.view.View
import com.example.vocab.databinding.FragmentTeacherBaseListBinding
import com.example.vocab.R
import com.example.vocab.basic.Word

/**
 * GUI component with text and check box
 */
data class BaseList(val list: Word): BaseItem<FragmentTeacherBaseListBinding> {
    override val layoutId: Int = R.layout.fragment_teacher_base_list
    override val itemID: Any = list.list

    private lateinit var binding: FragmentTeacherBaseListBinding

    private var postChecked = false

    var isChecked = false
        set(new){
            field=new
            binding.checked.isChecked = new
        }
        get(){
            return binding.checked.isChecked
        }

    var shape: Drawable? = null

    fun postChecked(checked: Boolean){
        postChecked = checked
    }

    override fun initViewBinding(view: View): FragmentTeacherBaseListBinding {
        return FragmentTeacherBaseListBinding.bind(view)
    }

    override fun getBinding(binding: FragmentTeacherBaseListBinding) {
        this.binding = binding

        binding.listName.text = list.list
        binding.checked.isChecked = postChecked

        shape?.let{
            binding.root.background = shape
        }
    }
}