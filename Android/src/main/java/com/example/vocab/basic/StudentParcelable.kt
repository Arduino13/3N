package com.example.vocab.basic

import android.os.Parcel
import android.os.Parcelable
import java.lang.Exception
import java.util.*

/**
 * Class that extends Student and is used to transfer student data between activities in application
 */
class StudentParcelable : Student, Parcelable {
    val exception = Exception("Internal error parcelable")

    /**
     * Function for writing objects waiting for synchronization to parcel
     *
     * @param T to determine how to correctly cast the object
     * [obj] object for which we want to know if it's waiting for synchronization
     * [add] if we want to know that any new web is waiting for synchronization, or any deleted one
     * [remote] if it's waiting for synchronization with local or remote database
     */
    private fun <T: Syncable> isToSync(obj: Syncable, add: Boolean, remote: Boolean): Boolean{
        var list = listOf<T>()
        var tempMap = mapOf<StudentSync.Actions, List<Syncable>>()

        tempMap = if(!remote) {
            localSync()
        } else{
            remoteSync()
        }

        list = if(add){
            tempMap[StudentSync.Actions.Add]!!.filter {
                it.getType() == obj.getType()
            }.map {
                it as T
            }
        } else{
            tempMap[StudentSync.Actions.Remove]!!.filter {
                it.getType() == obj.getType()
            }.map {
                it as T
            }
        }

        return list.contains(obj)
    }

    /**
     * Converts immutable list of objects for synchronize to mutable one
     */
    private fun convertToMutable(map: Map<StudentSync.Actions, List<Syncable>>):
            Map<StudentSync.Actions, MutableList<Syncable>>{
        val toSave = mutableMapOf<Actions, MutableList<Syncable>>()
        for((action, list) in map){
            toSave[action] = list.toMutableList()
        }

        return toSave
    }

    /**
     * Reads from parcel if object need to be synchronized, and used internal methods to write [obj]
     * to lists for synchronization
     */
    private fun readSyncParameters(obj: Syncable, parcel: Parcel){
        val localList = convertToMutable(localSync())
        val remoteList = convertToMutable(remoteSync())

        if(parcel.readString() == "true") localList[StudentSync.Actions.Add]!!.add(obj)
        if(parcel.readString() == "true") localList[StudentSync.Actions.Remove]!!.add(obj)
        if(parcel.readString() == "true") remoteList[StudentSync.Actions.Add]!!.add(obj)
        if(parcel.readString() == "true") remoteList[StudentSync.Actions.Remove]!!.add(obj)

        setListLocal(localList)
        setListRemote(remoteList)
    }

    constructor(student: Student): super(
        student.id, student.name, student.listWords, student.listWebs, student.listHomework, student.class_id,
        false, student.listTests
    ){
        setListLocal(student.localSync())
        setListRemote(student.remoteSync())
    }

    constructor(parcel: Parcel): super(){
        val id = parcel.readString() ?: throw exception
        val name = parcel.readString() ?: throw exception
        val class_id = parcel.readString() ?: throw exception
        val listWords = unparcelListOfWords(parcel, parcel.readInt(), true)

        val listOfWebs = mutableListOf<Web>()
        val websSize = parcel.readInt()
        for(i in 0 until websSize){
            val id = parcel.readString() ?: throw exception
            val address = parcel.readString() ?: throw exception
            val name = parcel.readString() ?: throw exception
            val class_id = parcel.readString() ?: throw exception
            listOfWebs.add(
                Web(
                    id,
                    address,
                    name,
                    class_id
                )
            )

            readSyncParameters(listOfWebs.last(), parcel)
        }

        val listOfHomework = mutableListOf<HomeWork>()
        val homeworkSize = parcel.readInt()
        for(i in 0 until homeworkSize) {
            val id = parcel.readString() ?: throw exception
            val name = parcel.readString() ?: throw exception
            val date = Date(parcel.readLong()) ?: throw exception
            val wordList = unparcelListOfWords(parcel, parcel.readInt())
            val dateCompleted = Date(parcel.readLong()) ?: throw exception
            val class_id = parcel.readString() ?: throw exception
            val internal_id = parcel.readString() ?: throw exception
            val completed = (parcel.readString() ?: throw exception) == "true"
            val completdWords = unparcelListOfWords(parcel, parcel.readInt())
            listOfHomework.add(
                HomeWork(
                    id,
                    name,
                    date,
                    wordList,
                    dateCompleted,
                    class_id,
                    internal_id,
                    completed,
                    completdWords
                )
            )

            readSyncParameters(listOfHomework.last(), parcel)
        }

            val listOfTests = mutableListOf<Test>()
            val sizeOfTests = parcel.readInt()
            for(i in 0 until sizeOfTests){
                val id = parcel.readString() ?: throw exception
                val words = unparcelListOfWords(parcel, parcel.readInt())
                val class_id = parcel.readString() ?: throw exception
                val date = Date(parcel.readLong())
                val wrongWords = unparcelListOfWords(parcel, parcel.readInt())
                listOfTests.add(
                    Test(
                        id,
                        words,
                        class_id,
                        date,
                        wrongWords
                    )
                )

                readSyncParameters(listOfTests.last(), parcel)
            }

            super.id = id
            super.name = name
            super.listWords = listWords.toMutableList()
            super.listWebs = listOfWebs
            super.listHomework = listOfHomework
            super.class_id = class_id
            super.listTests = listOfTests
    }

    /**
     * Writes list of words to parcel
     *
     * [withToSyncParameter] if we want to write information about synchronization to parcel
     */
    fun parcelListOfWords(listOfWords: List<Word>, parcel: Parcel, withToSyncParameter: Boolean = false){
        for(word in listOfWords){
            parcel.writeString(word.from)
            parcel.writeString(word.to)
            parcel.writeString(word.list)
            parcel.writeString(word.language)
            parcel.writeString(word.class_id)
            parcel.writeString(word.id)
            parcel.writeString(word.newList.toString())
            parcel.writeInt(word.stats.numSucces)
            parcel.writeInt(word.stats.numFail)
            parcel.writeLong(word.stats.lastTested.time)

            if(withToSyncParameter) {
                parcel.writeString(isToSync<Word>(word, add = true, remote = false).toString())
                parcel.writeString(isToSync<Word>(word, add = false, remote = false).toString())
                parcel.writeString(isToSync<Word>(word, add = true, remote = true).toString())
                parcel.writeString(isToSync<Word>(word, add = false, remote = true).toString())
            }
        }
    }

    /**
     * Reads word's list from parcel
     *
     * [size] number of words that'll be read from parcel
     * [withToSyncParameter] if we want to read information about synchronization from parcel
     */
    fun unparcelListOfWords(parcel: Parcel, size: Int, withToSyncParameter: Boolean = false): List<Word>{
        val toReturn = mutableListOf<Word>()

        for(i in 0 until size){
            val from = parcel.readString() ?: throw exception
            val to = parcel.readString() ?: throw exception
            val list = parcel.readString() ?: throw exception
            val language = parcel.readString() ?: throw exception
            val class_id = parcel.readString() ?: throw exception
            val id = parcel.readString() ?: throw exception
            val newList:Boolean = (parcel.readString() ?: throw exception)=="true"
            val numSucces = parcel.readInt()
            val numFail = parcel.readInt()
            val lastTested = Date(parcel.readLong())

            val stats = Word.WordStats(numFail, numSucces, lastTested)
            toReturn.add(Word(id, from, to, list, language, class_id, newList, stats))

            if(withToSyncParameter){
                readSyncParameters(toReturn.last(), parcel)
            }
        }

        return toReturn
    }

    /**
     * Extended function for writing to parcel
     *
     * Data to parcel are written sequentially, only using basic data types
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(super.id)
        parcel.writeString(super.name)
        parcel.writeString(super.class_id)

        parcel.writeInt(super.listWords.size)
        parcelListOfWords(super.listWords, parcel, true)

        parcel.writeInt(super.listWebs.size) //to know how many webs will need to by loaded when reading from parcel
        for(web in super.listWebs){
            parcel.writeString(web.id)
            parcel.writeString(web.address)
            parcel.writeString(web.name)
            parcel.writeString(web.class_id)
            parcel.writeString(isToSync<Web>(web, add = true, remote = false).toString())
            parcel.writeString(isToSync<Web>(web, add = false, remote = false).toString())
            parcel.writeString(isToSync<Web>(web, add = true, remote = true).toString())
            parcel.writeString(isToSync<Web>(web, add = false, remote = true).toString())
        }

        parcel.writeInt(super.listHomework.size)
        for(homework in super.listHomework){
            parcel.writeString(homework.id)
            parcel.writeString(homework.name)
            parcel.writeLong(homework.date.time)
            parcel.writeInt(homework.wordList.size)
            parcelListOfWords(homework.wordList, parcel)
            parcel.writeLong(homework.dateCompleted.time)
            parcel.writeString(homework.class_id)
            parcel.writeString(homework.homework_id)
            parcel.writeString(homework.completed.toString())
            parcel.writeInt(homework.completedWords.size)
            parcelListOfWords(homework.completedWords, parcel)
            parcel.writeString(isToSync<HomeWork>(homework, add = true, remote = false).toString())
            parcel.writeString(isToSync<HomeWork>(homework, add = false, remote = false).toString())
            parcel.writeString(isToSync<HomeWork>(homework, add = true, remote = true).toString())
            parcel.writeString(isToSync<HomeWork>(homework, add = false, remote = true).toString())
        }

        parcel.writeInt(super.listTests.size)
        for(test in super.listTests){
            parcel.writeString(test.id)
            parcel.writeInt(test.words.size)
            parcelListOfWords(test.words, parcel)
            parcel.writeString(test.class_id)
            parcel.writeLong(test.date.time)
            parcel.writeInt(test.wrongWords.size)
            parcelListOfWords(test.wrongWords, parcel)
            parcel.writeString(isToSync<Test>(test, add = true, remote = false).toString())
            parcel.writeString(isToSync<Test>(test, add = false, remote = false).toString())
            parcel.writeString(isToSync<Test>(test, add = true, remote = true).toString())
            parcel.writeString(isToSync<Test>(test, add = false, remote = true).toString())
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StudentParcelable> {
        override fun createFromParcel(parcel: Parcel): StudentParcelable {
            return StudentParcelable(parcel)
        }

        override fun newArray(size: Int): Array<StudentParcelable?> {
            return arrayOfNulls(size)
        }
    }
}
