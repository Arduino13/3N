package com.example.vocab.homework

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.FragmentResultListener
import com.example.vocab.R
import com.example.vocab.databinding.FragmentHorizontalHomeworkBinding
import java.util.*

/**
 * For view with calendar fragment
 *
 * @property homeworkDescriptionID id of action to move to [HomeworkDescription] fragment
 */
class HorizontalHomeworkFragment : GenericHomework<FragmentHorizontalHomeworkBinding>(){
    private lateinit var model: GenericHomeworkModel
    private var homeworkDescriptionID: Int? = null

    companion object{
        /**
         * Function for creating new instance and settings it's [model], with destination to move [homeworkDescriptionID]
         * on homework click
         *
         * Useful when we want to create fragments dynamically
         */
        fun newInstance(model: GenericHomeworkModel, @IdRes homeworkDescriptionID: Int): HorizontalHomeworkFragment{
            val newObj = HorizontalHomeworkFragment()
            newObj.setModel(model, homeworkDescriptionID)

            return newObj
        }
    }

    private fun setModel(fragmentModel: GenericHomeworkModel,  @IdRes homeworkDescriptionID: Int){
        model = fragmentModel
        this.homeworkDescriptionID = homeworkDescriptionID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHorizontalHomeworkBinding.inflate(inflater, container, false)

        if(this::model.isInitialized) {

            initData(binding, binding.listHomework, model, homeworkDescriptionID!!)

            parentFragmentManager.setFragmentResultListener("date", viewLifecycleOwner,
                FragmentResultListener() { _, bundle ->
                    val date = Date(bundle.getLong("time"))
                    adapterT.submitList(setList(date, date))
                }
            )
        }

        return binding.root
    }
}