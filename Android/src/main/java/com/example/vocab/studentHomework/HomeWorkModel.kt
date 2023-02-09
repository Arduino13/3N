package com.example.vocab.studentHomework

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.vocab.basic.Student
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.homework.BaseHomework
import com.example.vocab.homework.GenericHomeworkModel
import java.lang.Exception

/**
 * Homework model for student
 *
 * @property data list of only-read [BaseHomework] objects
 */
class HomeWorkModel(private val model: globalModel): GenericHomeworkModel(){
    private val _data = MutableLiveData<List<BaseHomework>>()

    override val data: LiveData<List<BaseHomework>> = _data

    init{
        loadData()
    }

    override fun onSave(newObjects: List<BaseItem<*>>) {
        val listNewHomework = mutableListOf<BaseHomework>()
        listNewHomework.addAll(data.value ?: listOf())

        for(h in listNewHomework) {
            for(newObject in newObjects) {
                (newObject as? BaseHomework)?.homework?.let {
                    if (h.homework.id == it.id) listNewHomework.remove(h)
                }
            }
        }

        for(newObject in newObjects) {
            (newObjects as? BaseHomework)?.let {
                listNewHomework.add(it)
            }
        }

        _data.value = listNewHomework
    }

    override fun onDelete(deletedObjects: List<BaseItem<*>>) {
        throw Exception("you can't delete homeworks")
    }

    override fun onCommit(newObjects: List<BaseItem<*>>, deletedObjects: List<BaseItem<*>>) {
        val student = model.data.value as Student

        for(n in newObjects) {
            (n as? BaseHomework)?.homework?.let {
                student.updateHomework(it)
            }
        }

        model.setData(student)
    }

    override fun loadData() {
        val toSave = mutableListOf<BaseHomework>()

        for(h in (model.data.value as Student).listHomework){
            toSave += BaseHomework(h)
        }

        _data.value = toSave
    }
}