package com.example.vocab.teacherHome

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.DateUtils
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentTeacherHomeworkDescriptionBinding
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration
import com.example.vocab.studentHomework.HomeworkTableWords
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Fragment for displaying details of homework
 */
class TeacherHomeworkDescription : Fragment() {
    private lateinit var binding: FragmentTeacherHomeworkDescriptionBinding

    /**
     * Sets up list of students
     */
    private fun setUpList(fragmentModel: TeacherHomeworkModel, id: String): List<BaseStudent>{
        val list = mutableListOf<BaseStudent>()

        for(h in fragmentModel.data.value ?: throw Exception("internal error - inconsistent data")){
            if(h.itemID == id){
                val students = fragmentModel.getListStudentHomework(h)
                for((student, homework) in students){
                    val newItem = BaseStudent(Pair(student, homework))
                    newItem.resources = resources
                    newItem.shape = Tools.getCircleLayout(R.color.white, requireContext(), resources)

                    list.add(newItem)
                }
            }
        }

        return list
    }

    private fun loadData(fragmentModel: TeacherHomeworkModel, id: String){
        for(h in fragmentModel.data.value ?: throw Exception("internal error - inconsistent data")){
            if(h.itemID == id){
                binding.homeworkNameTeacher.text = h.homework.name
                binding.StartDate.text = String.format(
                    resources.getString(R.string.homework_from_date),
                    DateUtils.fromDate2StringShort(h.homework.date)
                )
                binding.EndDate.text = String.format(
                    resources.getString(R.string.homework_to_date),
                    DateUtils.fromDate2StringShort(h.homework.dateCompleted)
                )
                binding.Completed.text = String.format(
                    resources.getString(R.string.homework_completed),
                    if(h.homework.completed) resources.getString(R.string.homework_yes) else resources.getString(R.string.homework_no)
                )

                binding.Completed.setTextColor(
                    if(!h.homework.completed) ResourcesCompat.getColor(resources, R.color.red, null)
                    else ResourcesCompat.getColor(resources, R.color.green, null)
                )


                childFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.problematicWordsFragment,
                        HomeworkTableWords.newInstance(h.homework.problematicWords ?: listOf())
                    )
                    commit()
                }

                break
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeacherHomeworkDescriptionBinding.inflate(inflater, container, false)

        val fragmentModel: TeacherHomeworkModel by activityViewModels()
        val id = arguments?.getString("homeworkID") ?: throw Exception("internal error - null homework id")

        val adapterT = BaseListAdapter()
        with(binding.studentsListHomework) {
            adapter = adapterT
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(LinearSpacingDecoration(10, 5))
        }

        fragmentModel.data.observe(viewLifecycleOwner, Observer {
            adapterT.submitList(setUpList(fragmentModel, id))
        })

        loadData(fragmentModel, id)

        binding.deleteHomework.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(resources.getString(R.string.homework_delete_dialog_title))
                .setMessage(resources.getString(R.string.homework_delete_dialog_question))
                .setNegativeButton(resources.getString(R.string.button_base_negative_no)) { _,_ -> }
                .setPositiveButton(resources.getString(R.string.button_base_positive)) { _,_ ->
                    for(h in fragmentModel.data.value ?: throw Exception("internal error - inconsistent data")) {
                        if (h.itemID == id) {
                            val id = fragmentModel.requestAccess() ?: throw Exception("access denied teacher homework fragment")
                            fragmentModel.delete(listOf(h), id)
                            fragmentModel.commit()
                            fragmentModel.releaseAccess()

                            binding.root.findNavController().popBackStack()
                        }
                    }
                }.show()
        }

        binding.navBar.setOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        return binding.root
    }
}