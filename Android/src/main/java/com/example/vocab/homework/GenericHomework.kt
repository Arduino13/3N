package com.example.vocab.homework

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.vocab.R
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration
import java.util.*

/**
 * Class that is parent to HorizontalHomeworkFragment and VerticalHomeworkFragment, contains method
 * for setting list data and for their initialization
 *
 * @property descriptionHomeworkID id of action to move to [HomeworkDescription] fragment
 */
abstract  class GenericHomework<T: ViewBinding>: Fragment(){
    protected lateinit var fragmentModel: GenericHomeworkModel
    protected lateinit var binding: T
    protected lateinit var adapterT: BaseListAdapter
    private var descriptionHomeworkID: Int? = null

    /**
     * Returns list of BaseHomework objects with set touchListener to destination described by
     * [descriptionHomeworkID]
     */
    protected open fun setList(from: Date, to: Date): List<BaseItem<*>>{
        val toReturn = mutableListOf<BaseItem<*>>()

        for(h in fragmentModel.data.value ?: listOf()){
            if(h.homework.date <= to  && h.homework.dateCompleted >= from){
                val shape = ContextCompat.getDrawable(requireContext(), R.drawable.layout_circle)
                shape?.colorFilter = PorterDuffColorFilter(ResourcesCompat.getColor(resources, R.color.gray, null), PorterDuff.Mode.OVERLAY)

                h.layout = shape
                h.resources = resources
                h.touchListener = BaseHomework.TouchAdapter{
                    descriptionHomeworkID?.let {
                        val data = bundleOf("homeworkID" to h.itemID)
                        binding.root.findNavController().navigate(it, data)
                    }
                }

                toReturn += h
            }
        }

        return toReturn
    }

    /**
     * Initialize fragment
     */
    protected fun initData(binding: T, list: RecyclerView, fragmentModel: GenericHomeworkModel, @IdRes descriptionHomeworkID: Int){
        this.binding = binding

        this.fragmentModel = fragmentModel
        this.descriptionHomeworkID = descriptionHomeworkID

        adapterT = BaseListAdapter()

        with(list){
            adapter = adapterT
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(LinearSpacingDecoration(20,10))
        }
    }
}