package com.example.vocab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.database.Database

class globalModelFactory(private val database: Database, private val errFun: ()->Unit): ViewModelProvider.Factory{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return globalModel(database, errFun) as T
    }
}