package com.example.vocab.basic

import android.drm.DrmStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vocab.merge

/**
 * Main interface for all objects that can be synchronized
 */
interface Syncable {
    enum class Types(val type: String){
        Student("students"),
        Teacher("teachers"),
        Homework("homework"),
        WebPage("web_pages"),
        Class("class"),
        Test("tests"),
        Vocabulary("vocabulary")
    }

    /**
     * Returns type of object from enum list of Types in Syncable
     */
    fun getType(): Types

    /**
     * Returns id of object
     */
    fun getID(): String
}