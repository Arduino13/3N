package com.example.vocab.webPages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.vocab.basic.Student
import com.example.vocab.basic.Web
import com.example.vocab.databinding.FragmentWebListItemsCellBinding
import com.example.vocab.globalModel
import kotlinx.serialization.descriptors.PrimitiveKind

/**
 * Old implementation that need to be refactored to use [BaseListAdapter]
 */
class WebRecyclerViewList(private val model: globalModel) :  RecyclerView.Adapter<WebRecyclerViewList.ViewHolder>(){
    var student = model.data.value as Student
    var checkBoxes: MutableMap<Web,Boolean> = mutableMapOf()
        private set

    var editable: Boolean = false
        set(new){
            field = new
            notifyDataSetChanged()
        }

    private var checkBoxToSet: CheckBox? = null
    private var checkBoxToSetState: Boolean? = null

    fun setCheckbox(view: CheckBox, state: Boolean){
        checkBoxToSet = view
        checkBoxToSetState = state

        notifyDataSetChanged()
    }

    fun loadData(){
        checkBoxToSet = null
        checkBoxToSetState = null

        student = model.data.value as Student
        checkBoxes.clear()
        for(w in student.listWebs){
            checkBoxes[w] = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebRecyclerViewList.ViewHolder {
        val binding = FragmentWebListItemsCellBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WebRecyclerViewList.ViewHolder, position: Int){
        holder.bind(student.listWebs[position], position, checkBoxToSet, checkBoxToSetState)
    }

    inner class ViewHolder(private val binding: FragmentWebListItemsCellBinding) : RecyclerView.ViewHolder(binding.root) {
        var page: Web? = null
            private set
        fun bind(web: Web, position: Int, checkBoxToSet: CheckBox? = null, checkBoxToSetState: Boolean? = null){
            binding.name = web.name
            page = web

            if(editable){
                binding.selected.visibility = View.VISIBLE
                binding.selected.setOnClickListener {
                    checkBoxes[web] = !checkBoxes[web]!!
                }

                if(checkBoxToSet != null && checkBoxToSetState != null){
                    if(binding.selected == checkBoxToSet){
                        binding.selected.isChecked = checkBoxToSetState
                        checkBoxes[web] = !checkBoxes[web]!!
                    }
                }
            }
            else{
                binding.selected.visibility = View.GONE
                binding.selected.isChecked = false
                binding.selected.setOnClickListener{}
            }
        }
    }

    override fun getItemCount() = student.listWebs.size
}