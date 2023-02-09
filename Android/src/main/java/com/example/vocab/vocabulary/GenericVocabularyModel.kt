package com.example.vocab.vocabulary

import androidx.lifecycle.LiveData
import com.example.vocab.gui.BaseTextCell
import com.example.vocab.gui.BaseViewModel

abstract class GenericVocabularyModel: BaseViewModel(){
    abstract  val lists: LiveData<List<BaseTextCell>?>
    abstract  val words: LiveData<List<BaseWord>?>
    abstract  val id: LiveData<String>
}