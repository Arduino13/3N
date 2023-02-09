package com.example.vocab.teacherVocabulary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vocab.basic.Word
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseTextCell
import com.example.vocab.vocabulary.BaseWord
import com.example.vocab.vocabulary.GenericVocabularyModel

/**
 * Teacher's vocabulary model
 *
 * @property lists read only word's lists
 * @property words read only word's list
 * @property id read only teacher's id
 */

class TeacherVocabularyModel(private val model: globalModel): GenericVocabularyModel() {
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
                    }
                    else{
                        newListOfWords.remove(w)
                    }
                }

                if(toDelete){
                    for(l in lists.value ?: listOf()){
                        if(l.text == listToDelete) newListOfLists.remove(l)
                    }
                }
            }
        }

        setData(newListOfLists, newListOfWords, id.value!!)
    }

    private fun setData(lists: List<BaseTextCell>, words: List<BaseWord>, id: String){
        _lists.value = lists
        _words.value = words
        _id.value = id
    }

    override fun onCommit(newObjects: List<BaseItem<*>>, deletedObjects: List<BaseItem<*>>) {
        if(newObjects.isNotEmpty() || deletedObjects.isNotEmpty()) {
            val toSaveWords = mutableListOf<Word>()
            val toDeleteWords = mutableListOf<Word>()

            for (newObj in newObjects) {
                (newObj as? BaseWord)?.let {
                    toSaveWords.add(it.word)
                }
            }

            for (delObj in deletedObjects) {
                (delObj as? BaseWord)?.let {
                    toDeleteWords.add(it.word)
                }
            }

            if(toSaveWords.isNotEmpty()) model.saveWords(toSaveWords)
            if(toDeleteWords.isNotEmpty()) model.removeWords(toDeleteWords)
        }
    }

    override fun loadData() {
        val selectedClass =
            model.classSelected.value ?: throw Exception("internal error class not selected")

        for (s in selectedClass.students) {
            val loadedListsNames = mutableListOf<String>()
            val lists = mutableListOf<BaseTextCell>()
            val words = mutableListOf<BaseWord>()

            for (w in s.listWords) {
                if (!loadedListsNames.contains(w.list)) {
                    lists += BaseTextCell(w.list)
                    loadedListsNames += w.list
                }
                words += BaseWord(w)
            }

            setData(lists, words, model.id.value!!)

            break
        }
    }
}