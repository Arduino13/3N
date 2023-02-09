package com.example.vocab.homework

import androidx.lifecycle.LiveData
import com.example.vocab.gui.BaseViewModel

abstract class GenericHomeworkModel: BaseViewModel(){
   abstract val data: LiveData<List<BaseHomework>>
}