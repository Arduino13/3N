package com.example.vocab.homework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.globalModel
import com.example.vocab.studentHomework.HomeWorkModel
import com.example.vocab.teacherHome.TeacherHomeworkModel
import java.lang.Exception

class HomeWorkModelFactory(private val model: globalModel): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            HomeWorkModel::class.java -> HomeWorkModel(
                model
            ) as T
            TeacherHomeworkModel::class.java -> TeacherHomeworkModel(model) as T
            else -> throw Exception("unsupported type - homeworkModelFactory")
        }
    }
}