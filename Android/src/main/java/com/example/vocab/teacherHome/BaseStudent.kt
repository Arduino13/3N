package com.example.vocab.teacherHome

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.view.View
import com.example.vocab.databinding.FragmentTeacherHomeworkDescriptionCellBinding
import com.example.vocab.gui.BaseItem
import com.example.vocab.R
import com.example.vocab.basic.HomeWork
import com.example.vocab.basic.Student

/**
 * GUI component which displays students name, number of learned words and if homework was completed
 */
data class BaseStudent(private val student: Pair<Student, HomeWork>): BaseItem<FragmentTeacherHomeworkDescriptionCellBinding> {
    override val layoutId: Int = R.layout.fragment_teacher_homework_description_cell
    override val itemID: Any = student.first.id

    var resources: Resources? = null
    var shape: Drawable? = null

    override fun initViewBinding(view: View): FragmentTeacherHomeworkDescriptionCellBinding {
        return FragmentTeacherHomeworkDescriptionCellBinding.bind(view)
    }

    override fun getBinding(binding: FragmentTeacherHomeworkDescriptionCellBinding) {
        resources?.let {
            binding.studentName.text = student.first.name
            binding.wordRation.text = String.format(
                it.getString(R.string.homework_students_word_ration),
                student.second.completedWords.size,
                student.second.wordList.size
            )
            binding.completedImageView.setImageResource(
                if(student.second.completed) R.drawable.homework_yes else R.drawable.homework_no
            )

            shape?.let{
                binding.root.background = shape
            }
        }
    }
}