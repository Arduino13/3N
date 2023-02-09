package com.example.vocab.vocabulary

import androidx.lifecycle.ViewModel

/**
 * Helper model to transfer data between fragments
 *
 * @property accessId id to access model data
 * @property listNameSavedInstance used when creating new word's list
 */
class VocabularySettingModel: ViewModel(){
    var accessId: String? = null
    var listNameSavedInstance: String? = null
}