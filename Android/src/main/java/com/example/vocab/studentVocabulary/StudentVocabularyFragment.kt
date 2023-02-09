package com.example.vocab.studentVocabulary

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.R
import com.example.vocab.globalModel
import com.example.vocab.teacherVocabulary.TeacherVocabularyModel
import com.example.vocab.vocabulary.GenericVocabularyModel
import com.example.vocab.vocabulary.VocabularyFragment
import com.example.vocab.vocabulary.VocabularyModelFactory

class StudentVocabularyFragment : VocabularyFragment(destList = R.id.toVocabularyList, destNewList = R.id.toNewList){
    override fun getFragmentModelObj(): GenericVocabularyModel {
        val model: globalModel by activityViewModels()
        val factory = VocabularyModelFactory(model)

        return ViewModelProvider(requireActivity(), factory).get(VocabularyModel::class.java)
    }
}