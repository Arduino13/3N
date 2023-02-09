package com.example.vocab.teacherVocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.R
import com.example.vocab.globalModel
import com.example.vocab.vocabulary.GenericVocabularyModel
import com.example.vocab.vocabulary.VocabularyAddListWords
import com.example.vocab.vocabulary.VocabularyModelFactory

class TeacherVocabularyAddListWords: VocabularyAddListWords(destNewWord = R.id.toNewWordTeacher) {
    override fun getFragmentModelObj(): GenericVocabularyModel {
        val model: TeacherVocabularyModel by activityViewModels()
        return model
    }
}