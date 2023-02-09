package com.example.vocab.homework

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentHomeworkBinding
import com.example.vocab.globalModel
import com.example.vocab.studentHomework.HomeWorkModel

/**
 * Main homework fragment which is opened in both student's and teacher's part of application,
 * this fragment than decides if it should display [HorizontalHomeworkFragment]
 * with [CalendarHomeworkFragment] or [VerticalHomeworkFragment], the main difference between
 * student's and teacher's part is in homework description fragment and used model
 */
class HomeworkFragment : Fragment() {
    private lateinit var binding: FragmentHomeworkBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeworkBinding.inflate(inflater, container, false)

        val model: globalModel by activityViewModels()
        val factory = HomeWorkModelFactory(model)
        val fragmentModel = ViewModelProvider(requireActivity(), factory).get(HomeWorkModel::class.java)

        if(Tools.isVertical(resources)){
            val ft = childFragmentManager.beginTransaction()
            ft.replace(R.id.homeworkPlaceholder, VerticalHomeworkFragment.newInstance(fragmentModel, R.id.toHomeworkDescription))
            ft.commit()

            binding.calendarPlaceholder.visibility = View.GONE
            binding.divider.visibility = View.GONE
        }
        else{
            val ft = childFragmentManager.beginTransaction()
            ft.replace(R.id.homeworkPlaceholder, HorizontalHomeworkFragment.newInstance(fragmentModel, R.id.toHomeworkDescription))
            ft.commit()

            binding.calendarPlaceholder.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
        }

        return binding.root
    }
}