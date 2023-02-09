package com.example.vocab.teacherHome

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.databinding.FragmentTeacherHomeBinding
import com.example.vocab.globalModel
import com.example.vocab.homework.HomeWorkModelFactory
import com.example.vocab.homework.HorizontalHomeworkFragment
import com.example.vocab.homework.VerticalHomeworkFragment

/**
 * Main screen which is displayed in teacher's part of application
 */
class TeacherHomeFragment : Fragment() {
    private lateinit var binding: FragmentTeacherHomeBinding

    private fun isVertical(): Boolean{
        return resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherHomeBinding.inflate(inflater, container, false)

        val model: globalModel by activityViewModels()
        val factory = HomeWorkModelFactory(model)
        val fragmentModel = ViewModelProvider(requireActivity(), factory).get(TeacherHomeworkModel::class.java)

        if(isVertical()){
            val ft = childFragmentManager.beginTransaction()
            ft.replace(R.id.homeworkPlaceholder, VerticalHomeworkFragment.newInstance(fragmentModel, R.id.toHomeworkDescriptionTeacher))
            ft.commit()

            binding.calendarPlaceholder.visibility = View.GONE
            binding.divider.visibility = View.GONE
        }
        else{
            val ft = childFragmentManager.beginTransaction()
            ft.replace(R.id.homeworkPlaceholder, HorizontalHomeworkFragment.newInstance(fragmentModel, R.id.toHomeworkDescriptionTeacher))
            ft.commit()

            binding.calendarPlaceholder.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
        }

        binding.addHomework.setOnClickListener {
            binding.root.findNavController().navigate(R.id.toAddHomework)
        }

        return binding.root
    }
}