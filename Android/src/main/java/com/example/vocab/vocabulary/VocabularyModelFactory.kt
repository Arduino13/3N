package com.example.vocab.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.globalModel
import com.example.vocab.studentVocabulary.VocabularyModel
import com.example.vocab.teacherVocabulary.TeacherVocabularyModel

class VocabularyModelFactory(private val model: globalModel): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass){
            VocabularyModel::class.java -> VocabularyModel(model) as T
            TeacherVocabularyModel::class.java -> TeacherVocabularyModel(model) as T
            else -> throw Exception("unsupported type vocabularymodelfactory")
        }
    }
}