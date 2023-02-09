package com.example.vocab.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vocab.database.Database

class SettingFactoryModel(private val db: Database): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingModel(db) as T
    }
}