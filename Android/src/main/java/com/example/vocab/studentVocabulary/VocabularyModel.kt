package com.example.vocab.studentVocabulary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vocab.basic.Student
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseTextCell
import com.example.vocab.vocabulary.BaseWord
import com.example.vocab.vocabulary.GenericVocabularyModel

/**
 * Student's model for vocabulary
 *
 * @property lists read only list of word's lists
 * @property words read only list of words
 * @property id read only student id
 */

class VocabularyModel(private val model: globalModel): GenericVocabularyModel(){
    private val _lists = MutableLiveData<List<BaseTextCell>?>()
    private val _words = MutableLiveData<List<BaseWord>?>()
    private val _id = MutableLiveData<String>()

    override val lists: LiveData<List<BaseTextCell>?> = _lists
    override val words: LiveData<List<BaseWord>?> = _words
    override val id: LiveData<String> = _id

    init{
        loadData()
    }

    override fun onSave(newObjects: List<BaseItem<*>>){
        val newListOfWords = mutableListOf<BaseWord>()
        newListOfWords.addAll(words.value ?: listOf())

        for(newObject in newObjects) {
            (newObject as? BaseWord)?.word?.let {
                newListOfWords += BaseWord(it)
            }
        }

        setData(_lists.value ?: listOf(), newListOfWords, id.value!!)
    }

    override fun onDelete(deletedObjects: List<BaseItem<*>>){
        val newListOfLists = mutableListOf<BaseTextCell>()
        newListOfLists.addAll(lists.value ?: listOf())
        val newListOfWords = mutableListOf<BaseWord>()
        newListOfWords.addAll(words.value ?: listOf())

        for (word in deletedObjects) {
            (word as? BaseWord)?.word?.let {
                val listToDelete = it.list
                var toDelete = true

                for (w in words.value ?: listOf()) {
                    if (w.word.id != it.id) {
                        if (w.word.list == listToDelete) toDelete = false
                    } else {
                        newListOfWords.remove(w)
                    }
                }

                if (toDelete) {
                    for (l in lists.value ?: listOf()) {
                        if (l.text == listToDelete) newListOfLists.remove(l)
                    }
                }
            }
        }

        setData(newListOfLists, newListOfWords, id.value!!)
    }

    override fun onCommit(newObjects: List<BaseItem<*>>, deletedObjects: List<BaseItem<*>>){
        if(newObjects.isNotEmpty() || deletedObjects.isNotEmpty()) {
            val student = model.data.value as Student

            for (n in newObjects) {
                (n as? BaseWord)?.word?.let {
                    student.addWord(it)
                }
            }
            for (d in deletedObjects) {
                (d as? BaseWord)?.word?.let {
                    student.removeWord(it)
                }
            }

            model.setData(student)
        }
    }

    private fun setData(lists: List<BaseTextCell>, words: List<BaseWord>, id: String){
        _lists.value = lists
        _words.value = words
        _id.value = id
    }

    override fun loadData(){
        val student = model.data.value as Student
        val loadedListsNames = mutableListOf<String>()
        val lists = mutableListOf<BaseTextCell>()
        val words = mutableListOf<BaseWord>()

        for(w in student.listWords){
            if(!loadedListsNames.contains(w.list)){
                lists += BaseTextCell(w.list)
                loadedListsNames += w.list
            }
            words += BaseWord(w)
        }

        setData(lists,words, student.id)
    }
}
