package com.example.vocab.teacherHome

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vocab.basic.HomeWork
import com.example.vocab.basic.OpenHomework
import com.example.vocab.basic.Student
import com.example.vocab.basic.Word
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.homework.BaseHomework
import com.example.vocab.homework.GenericHomeworkModel

/**
 * Model for homework tab
 *
 * @property data list of only-read [BaseHomework] objects
 */

class TeacherHomeworkModel(private val model: globalModel): GenericHomeworkModel() {
    private val _data = MutableLiveData<List<BaseHomework>>()
    private val _id = MutableLiveData<String>()

    override val data: LiveData<List<BaseHomework>> = _data
    val id: LiveData<String> = _id

    var listOfWordsToAdd = MutableLiveData<List<Word>>()
    var wordsToAdd = MutableLiveData<List<Word>>()

    init{
        loadData()
    }

    /**
     * For given [homework] returns map with keys as students and values as their homework objects
     */
    fun getListStudentHomework(homework: BaseHomework): Map<Student, HomeWork>{
        val classSelected = model.classSelected.value ?: throw Exception("internal error - class not selected")
        val mapToReturn = mutableMapOf<Student, HomeWork>()

        for(s in classSelected.students){
            mapToReturn[s] = s.getHomeworkByHomeworkID(homework.homework.homework_id)!!
        }

        return mapToReturn
    }

    override fun onSave(newObjects: List<BaseItem<*>>) {
        val listToSubmit = mutableListOf<BaseHomework>()
        listToSubmit.addAll(data.value ?: listOf())

        for(newObject in newObjects) {
            (newObject as? BaseHomework)?.let {
                listToSubmit.add(it)
            }
        }

        _data.postValue(listToSubmit)
    }

    override fun onDelete(deletedObjects: List<BaseItem<*>>) {
        val listToSubmit = mutableListOf<BaseHomework>()
        listToSubmit.addAll(data.value ?: listOf())

        for(obj in deletedObjects){
            (obj as? BaseHomework)?.let{
                listToSubmit.remove(it)
            }
        }

        _data.postValue(listToSubmit)
    }

    override fun onCommit(newObjects: List<BaseItem<*>>, deletedObjects: List<BaseItem<*>>) {
        for(obj in newObjects){
            (obj as? BaseHomework)?.let{
                model.saveHomework(it.homework)
            }
        }

        for(obj in deletedObjects){
            (obj as? BaseHomework)?.let {
                model.removeHomework(it.homework)
            }
        }
    }

    override fun loadData() {
        val classSelected = model.classSelected.value ?: throw Exception("internal error - class not selected")
        val listToSubmit = mutableListOf<BaseHomework>()

        if(classSelected.students.isNotEmpty()){
            for(h in classSelected.students.first().listHomework){
                var completed = true
                for(s in classSelected.students){
                    if(s.getHomework(h.getID())?.completed != true) completed = false
                }

                val homeworkToSave = OpenHomework(h)
                homeworkToSave.completed = completed
                homeworkToSave.completedWords = listOf()

                listToSubmit.add(BaseHomework(homeworkToSave))
            }
        }

        _data.value = listToSubmit
        _id.value = model.id.value!!
    }
}