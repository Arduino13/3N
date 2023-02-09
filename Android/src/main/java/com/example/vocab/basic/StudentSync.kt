package com.example.vocab.basic

import com.example.vocab.merge

/**
 * Class that handles object synchronization within Student class
 *
 * @constructor initializes arrays for capturing object changes
 */
abstract class StudentSync{
    enum class Actions {
        Add,
        Remove
    }

    private var toSync = mutableMapOf<Actions,MutableList<Syncable>>()
    private var toSyncR = mutableMapOf<Actions,MutableList<Syncable>>()

    init{
        toSync[Actions.Remove] = mutableListOf()
        toSync[Actions.Add] = mutableListOf()
        toSyncR[Actions.Remove] = mutableListOf()
        toSyncR[Actions.Add] = mutableListOf()
    }

    /**
     * if object is added to student class (new homework is assigned, new word is created) this method
     * stores it to toSyncR and to toSync with action to add this object to database
     *
     * [obj] object to synchronize
     * [local] if function should add object to synchronize only for local database on default false
     */
    protected fun addToSync(obj: Syncable, local: Boolean = false){
        if(obj.getType() in Syncable.Types.values() && toSync[Actions.Add]?.contains(obj) != true){
            toSync.merge(Actions.Add, mutableListOf(obj))
            if(!local) {
                toSyncR.merge(Actions.Add, mutableListOf(obj))
            }
        }
    }

    /**
     * if object is removed from student class (word is deleted, homework is deleted), it's copy is stored
     * inside toSyncR and toSync with action to remove this object from database
     *
     * [obj] object to synchronize
     * [local] if function should add object to synchronize only for local database on default false
     */
    protected fun removeToSync(obj: Syncable, local: Boolean = false){
        if(obj.getType() in Syncable.Types.values() && toSync[Actions.Remove]?.contains(obj) != true){
            toSync.merge(Actions.Remove, mutableListOf(obj))
            if(!local) {
                toSyncR.merge(Actions.Remove, mutableListOf(obj))
            }
        }
    }

    /**
     * @return returns objects to synchronize with remote database
     */
    fun remoteSync(): Map<Actions,List<Syncable>>{
        return toSyncR
    }

    /**
     * @return returns objects to synchronize with local database
     */
    fun localSync(): Map<Actions,List<Syncable>>{
        return toSync
    }

    /**
     * initializes list of objects to synchronize for local database from external list
     * used in StudentParcelable class
     *
     * [local] map with pair action-object
     */
    protected fun setListLocal(local: Map<Actions,List<Syncable>>){
        val toSave = mutableMapOf<Actions, MutableList<Syncable>>()
        for((action, list) in local){
            toSave[action] = list.toMutableList()
        }

        toSync = toSave
    }

    /**
     * initializes list of objects to synchronize for remote database from external list
     * used in StudentParcelable class
     *
     * [Remote] map with pair action-object
     */
    protected fun setListRemote(Remote: Map<Actions,List<Syncable>>){
        val toSave = mutableMapOf<Actions, MutableList<Syncable>>()
        for((action, list) in Remote){
            toSave[action] = list.toMutableList()
        }

        toSyncR = toSave
    }

    /**
     * Clears list of objects to synchronize for local database
     */
    fun localSyncDel(){
        toSync.clear()
        toSync[Actions.Remove] = mutableListOf()
        toSync[Actions.Add] = mutableListOf()
    }

    /**
     * Clears list of objects to synchronize for remote database
     */
    fun remoteSyncDel(){
        toSyncR.clear()
        toSyncR[Actions.Remove] = mutableListOf()
        toSyncR[Actions.Add] = mutableListOf()
    }

    /**
     * Checks if any object is pending to synchronize with remote database
     *
     * @return true if such objects exists
     */
    fun isToSyncRemote(): Boolean{
        return toSyncR[Actions.Add]!!.size !=0 || toSyncR[Actions.Remove]!!.size !=0
    }

    /**
     * Checks if any object is pending to synchronize with remote database
     *
     * @return true if such objects exists
     */
    fun isToSyncLocal(): Boolean{
        return toSync[Actions.Add]!!.size !=0 || toSync[Actions.Remove]!!.size !=0
    }
}