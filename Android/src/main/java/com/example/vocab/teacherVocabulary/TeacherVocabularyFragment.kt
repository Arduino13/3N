package com.example.vocab.teacherVocabulary

import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.R
import com.example.vocab.globalModel
import com.example.vocab.vocabulary.GenericVocabularyModel
import com.example.vocab.vocabulary.VocabularyFragment
import com.example.vocab.vocabulary.VocabularyModelFactory

class TeacherVocabularyFragment : VocabularyFragment(destList = R.id.toWordListTeacher, destNewList = R.id.toNewListTeacher){
    override fun getFragmentModelObj(): GenericVocabularyModel {
        val model: globalModel by activityViewModels()
        val factory = VocabularyModelFactory(model)

        return ViewModelProvider(requireActivity(), factory).get(TeacherVocabularyModel::class.java)
    }
}