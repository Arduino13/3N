package com.example.vocab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vocab.basic.*
import com.example.vocab.database.Database
import kotlinx.coroutines.runBlocking

/**
 * Main model that holds student's or teacher's data and writing to it equals writing to database,
 * an opposite for fragment models which are by default only temporally
 *
 * @property data student or teacher
 * @property id student's id or teacher's id
 * @property classSelected used in teacher's part of application to know current selected class
 */

class globalModel(private val DB: Database, private val errFun: ()->Unit): ViewModel(){
    private val _data = MutableLiveData<Syncable>()
    private val _id = MutableLiveData<String>()
    private val _classSelected = MutableLiveData<Class?>()

    val data: LiveData<Syncable> = _data
    var id: LiveData<String> = _id
    val classSelected: LiveData<Class?> = _classSelected

    /**
     * reloads data
     */
    fun reInit(){
        runBlocking {
            try {
                val logInfo = DB.login()

                when {
                    logInfo?.get(Database.DBloginMessage.succes) == false -> {
                        errFun
                    }
                    logInfo?.get(Database.DBloginMessage.isTeacher) == true -> {
                        _data.value = DB.getTeacher() as Syncable
                    }
                    else -> {
                        _data.value = DB.getStudent() as Syncable
                    }
                }
            }catch(exception: Database.DatabaseException){
                if(exception.localizedMessage == "wrong initialization") {
                    errFun()
                }
                else{
                    throw exception
                }
            }
        }
    }

    init{
       reInit()
    }

    fun setDataMainThread(data: Syncable){
        _data.value = data
        if(classSelected.value != null) _id.value = classSelected.value!!.id else data.getID()

        if(data.getType() == Syncable.Types.Student){
            DB.saveStudent(data as Student)
        }
    }

    /**
     * writes data to database
     */
    fun setData(data: Syncable){
        _data.postValue(data)
        if(classSelected.value != null) _id.value = classSelected.value!!.id else data.getID()

        if(data.getType() == Syncable.Types.Student){
            DB.saveStudent(data as Student)
        }
    }

    /**
     * sets selected class
     */
    fun selectClass(classSelected: Class? = null){
        _classSelected.value = classSelected

        classSelected?.let {
            _id.value = classSelected.id
        }
    }

    /**
     * save homework assigned by teacher
     */
    fun saveHomework(homework: HomeWork){
        if(data.value?.getType() != Syncable.Types.Teacher) throw Exception("this method can be used only with teacher object")

        _data.value = (DB.saveHomework(homework)) //maybe use coroutines i am not sure about performance
        _classSelected.value?.let{
            _classSelected.value = (_data.value as Teacher).getClass(it.id)
        }
    }

    /**
     * save words assigned by teacher
     */
    fun saveWords(words: List<Word>){
        if(data.value?.getType() != Syncable.Types.Teacher) throw Exception("this method can be used only with teacher object")

        _data.value = DB.addWords(words)
        _classSelected.value?.let{
            _classSelected.value = (_data.value as Teacher).getClass(it.id)
        }
    }

    /**
     * remove homework assigned by teacher
     */
    fun removeHomework(homework: HomeWork){
        if(data.value?.getType() != Syncable.Types.Teacher) throw Exception("this method can be used only with teacher object")

        _data.value = DB.removeHomework(homework)
        _classSelected.value?.let{
            _classSelected.value = (_data.value as Teacher).getClass(it.id)
        }
    }

    /**
     * remove word assigned by teacher
     */
    fun removeWords(words: List<Word>){
        if(data.value?.getType() != Syncable.Types.Teacher) throw Exception("this method can be used only with teacher object")

        _data.value = DB.removeWords(words)
        _classSelected.value?.let{
            _classSelected.value = (_data.value as Teacher).getClass(it.id)
        }
    }

    fun getRankList(onResult: (result: Map<String, Pair<Int,Int>>?) -> Unit){
        DB.getRankList { result ->
            onResult(result)
        }
    }
}