package com.example.vocab.gui

import androidx.lifecycle.ViewModel
import com.example.vocab.Tools
import java.lang.Exception

/**
 * Generic ViewModel for managing temporarily changes in models and saving them to global model
 *
 * @property newObjects list of objects which were added to the model
 * @property deletedObjects copy of object that were deleted from the model
 */
abstract class BaseViewModel(): ViewModel() {
    private val newObjects = mutableListOf<BaseItem<*>>()
    private val deletedObjects = mutableListOf<BaseItem<*>>()

    private var objectThatIsWriting: String? = null

    /**
     * Event for child class when save method is called
     */
    protected abstract fun onSave(newObjects: List<BaseItem<*>>)

    /**
     * Event for child class when delete method is called
     */
    protected abstract fun onDelete(deletedObjects: List<BaseItem<*>>)

    /**
     * Event for child class when commit method is called
     */
    protected abstract fun onCommit(newObjects: List<BaseItem<*>>, deletedObjects: List<BaseItem<*>>)

    /**
     * Event for child class to reload data
     */
    protected abstract fun loadData()

    /**
     * generates handler (id), to guarantee there is only one object at the time that is writing to
     * model, sometimes in case of multiple steps dialog for creating word for example,
     * there are multiple fragments with same model
     *
     * @return id as handler
     */
    fun requestAccess(): String?{
        return if(objectThatIsWriting == null){
            objectThatIsWriting = Tools.getUUID()
            return objectThatIsWriting
        } else{
            null
        }
    }

    /**
     * save new object to model, but no to global model
     */
    fun save(newObject: BaseItem<*>, obj: String){
        if(obj == objectThatIsWriting) {
            newObjects.add(newObject)
            onSave(listOf(newObject))
        }
        else throw Exception("Denied writing access to fragmentModel")
    }

    /**
     * saves a list of objects to model, but no to global model
     */
    fun save(newObjectsLocal: List<BaseItem<*>>, obj: String){
        if(obj == objectThatIsWriting){
            for(obj in newObjectsLocal){
                newObjects.add(obj)
            }

            onSave(newObjectsLocal)
        }
    }

    /**
     * deletes object from model, but not from global model
     */
    fun delete(deletedObjects: List<BaseItem<*>>, obj: String){
        if(obj == objectThatIsWriting){
            for(d in deletedObjects){
                if(d in newObjects) newObjects.remove(d)
                else this.deletedObjects.add(d)
            }

            onDelete(deletedObjects)
        }
        else throw Exception("Denied writing access to fragmentModel")
    }

    /**
     * commits all changes to global model and to database
     */
    fun commit(){
        onCommit(newObjects, deletedObjects)
    }

    /**
     * releasing access to model
     */
    fun releaseAccess(){
        objectThatIsWriting = null
        newObjects.clear()
        deletedObjects.clear()

        loadData()
    }
}