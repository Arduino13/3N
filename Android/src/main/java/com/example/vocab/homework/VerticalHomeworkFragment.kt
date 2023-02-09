package com.example.vocab.homework

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.DateUtils
import com.example.vocab.R
import com.example.vocab.databinding.FragmentVerticalHomeworkBinding
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration
import java.util.Date
import java.util.Calendar

/**
 * For view with calendar fragment
 *
 * @property homeworkDescriptionID id of action to move to [HomeworkDescription] fragment
 */
class VerticalHomeworkFragment : GenericHomework<FragmentVerticalHomeworkBinding>() {
    private val weekTime = 7*24*60*60*1000
    private lateinit var model: GenericHomeworkModel
    private var homeworkDescriptionID: Int? = null

    companion object{
        /**
         * Function for creating new instance and settings it's [model], with destination to move [homeworkDescriptionID]
         * on homework click
         *
         * Useful when we want to create fragments dynamically
         */
        fun newInstance(model: GenericHomeworkModel, @IdRes homeworkDescriptionID: Int): VerticalHomeworkFragment{
            val newObj = VerticalHomeworkFragment()
            newObj.setModel(model, homeworkDescriptionID)

            return newObj
        }
    }

    private fun setModel(model: GenericHomeworkModel, @IdRes homeworkDescriptionID: Int){
        this.model = model
        this.homeworkDescriptionID = homeworkDescriptionID
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentVerticalHomeworkBinding.inflate(inflater, container, false)

        if(this::model.isInitialized) {
            val settingModel: HomeworkSettingModel by activityViewModels()

            val view = binding.homeworkList
            initData(binding, view, model, homeworkDescriptionID!!)

            val date = settingModel.calendarCurrentDate ?: Date()

            val calendar = Calendar.getInstance()
            calendar.time = date
            var toMonday = calendar.get(Calendar.DAY_OF_WEEK) - 2
            if (toMonday < 0) {
                toMonday += 7
            }
            date.time = date.time - (toMonday * 24 * 60 * 60 * 1000)

            binding.calendarDate.text =
                "${DateUtils.fromDate2StringShort(date)}-${DateUtils.fromDate2StringShort(Date(date.time + weekTime))}"

            fragmentModel.data.observe(viewLifecycleOwner, Observer {
                adapterT.submitList(setList(date, Date(date.time + weekTime)))
            })

            binding.prevWeek.setOnClickListener {
                date.time = date.time - weekTime
                settingModel.calendarCurrentDate = date
                binding.calendarDate.text =
                    "${DateUtils.fromDate2StringShort(date)}-${DateUtils.fromDate2StringShort(
                        Date(date.time + weekTime)
                    )}"
                adapterT.submitList(setList(date, Date(date.time + weekTime)))
            }

            binding.nextWeek.setOnClickListener {
                date.time = date.time + weekTime
                settingModel.calendarCurrentDate = date
                binding.calendarDate.text =
                    "${DateUtils.fromDate2StringShort(date)}-${DateUtils.fromDate2StringShort(
                        Date(date.time + weekTime)
                    )}"
                adapterT.submitList(setList(date, Date(date.time + weekTime)))
            }
        }

        return binding.root
    }
}