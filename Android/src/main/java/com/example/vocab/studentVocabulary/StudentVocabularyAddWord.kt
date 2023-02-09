package com.example.vocab.studentVocabulary

import androidx.fragment.app.activityViewModels
import com.example.vocab.teacherVocabulary.TeacherVocabularyModel
import com.example.vocab.vocabulary.GenericVocabularyModel
import com.example.vocab.vocabulary.VocabularyAddWord

class StudentVocabularyAddWord: VocabularyAddWord() {
    override fun getFragmentModelObj(): GenericVocabularyModel {
        val model: VocabularyModel by activityViewModels()
        return model
    }
}