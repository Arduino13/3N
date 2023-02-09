package com.example.vocab.homework

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.vocab.R
import com.example.vocab.databinding.FragmentCalendarHomeworkBinding
import java.util.*

/**
 * Control class for fragment with calendar, when selected date is changed it's passed through
 * setFragmentResult to parent fragment
 */
class CalendarHomeworkFragment : Fragment() {
    private lateinit var binding: FragmentCalendarHomeworkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCalendarHomeworkBinding.inflate(inflater, container, false)

        val settingModel: HomeworkSettingModel by activityViewModels()

        settingModel.calendarCurrentDate?.let{
            binding.calendarHomework.date = it.time
            parentFragmentManager.setFragmentResult("date", bundleOf("time" to it.time))
        }

        binding.calendarHomework.setOnDateChangeListener { _, y, m, d ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.YEAR, y)
            calendar.set(Calendar.MONTH, m)
            calendar.set(Calendar.DAY_OF_MONTH, d)

            settingModel.calendarCurrentDate = calendar.time

            parentFragmentManager.setFragmentResult("date", bundleOf("time" to calendar.time.time))
        }

        return binding.root
    }
}