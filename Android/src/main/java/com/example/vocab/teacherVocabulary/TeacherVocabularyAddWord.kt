package com.example.vocab.teacherVocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.globalModel
import com.example.vocab.vocabulary.GenericVocabularyModel
import com.example.vocab.vocabulary.VocabularyAddWord
import com.example.vocab.vocabulary.VocabularyModelFactory

class TeacherVocabularyAddWord: VocabularyAddWord() {
    override fun getFragmentModelObj(): GenericVocabularyModel {
        val model: TeacherVocabularyModel by activityViewModels()
        return model
    }
}