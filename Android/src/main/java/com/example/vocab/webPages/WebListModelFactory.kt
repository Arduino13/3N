package com.example.vocab.webPages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.globalModel

class WebListModelFactory(private val model: globalModel, private val numRSS: Int): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return WebListModel(model,numRSS) as T
    }
}