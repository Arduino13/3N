package com.example.vocab.database

import android.app.Activity
import android.content.Context
import android.database.sqlite.SQLiteException
import com.example.vocab.Settings
import com.example.vocab.Tools
import com.example.vocab.VocabularyUtils
import com.example.vocab.basic.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.sql.SQLException
import java.util.*

/**
 * Main database class that combines functions from DB_loc and DB_rem, to save all data if possible
 * to local and to remote database
 *
 * @property context used by DB_rem class
 * @property activity used for storing shared preferences
 * @property id identification string of logged user
 * @property hash hash of logged user
 * @property loc local database
 * @property rem remote database
 * @property loggedIn if user is logged in
 * @property teacherPriv if logged user is a teacher
 *
 * @constructor initializes remote database
 */

class Database(
    private val context: Context,
    private val activity: Activity,
    private var id: String? = null,
    private var hash: String? = null
) {
    private val loc = DB_loc(context)
    private var rem: DB_rem? = null
    var loggedIn = false
        private set
    var teacherPriv = false
        private set

    class DBloginMessage {
        companion object {
            const val succes = "auth"
            const val isTeacher = "teacher"
            const val connectionError = "internet"
        }
    }

    class DatabaseException(message: String) : Exception(message)

    init {
        if (id != null && hash != null) {
            rem = DB_rem(id!!, hash!!, context.contentResolver)
        }
    }

    /**
     * Helper function to reduce redundancy in code, saves changes to remote database
     */
    private suspend fun syncStudentSubProcess(
        studentSync: Student,
        toRemove: List<Syncable>,
        toRemoveWords: List<String>
    ) {
        rem?.let {
            try {
                if (it.saveStudent(studentSync).succes) {
                    loc.deleteSync(
                        Syncable.Types.WebPage,
                        Syncable.Types.Homework,
                        Syncable.Types.Test,
                        Syncable.Types.Vocabulary
                    )
                }
                if (it.remove(toRemove).succes) {
                    loc.deleteSyncREM(
                        Syncable.Types.WebPage,
                        Syncable.Types.Homework
                    )
                }
                if (it.removeWords(toRemoveWords).succes) {
                    loc.deleteSyncREM(Syncable.Types.Vocabulary)
                }
            } catch (e: HttpConnector.HttpConnector_exception) {
                println("remote db error (sync_student) " + e.localizedMessage)
            }
        }

        if (rem == null) throw DatabaseException("internal error")
    }

    /**
     * Load objects that need to synchronized from local database and tries to write changes to
     * remote database
     *
     * [async] determines whether sending message to remote database will be handled by main thread
     * (blocking) or by IO thread (non-blocking)
     */
    private fun syncStudent(student: Student, async: Boolean = true) {
        if (loc.getSync(Syncable.Types.Student).isEmpty() &&
            loc.getSync(Syncable.Types.Teacher).isEmpty() &&
            loc.getSync(Syncable.Types.Homework).isEmpty() &&
            loc.getSync(Syncable.Types.Test).isEmpty() &&
            loc.getSync(Syncable.Types.WebPage).isEmpty() &&
            loc.getSync(Syncable.Types.Vocabulary).isEmpty() &&
            loc.getSyncREM(Syncable.Types.Student).isEmpty() &&
            loc.getSyncREM(Syncable.Types.Teacher).isEmpty() &&
            loc.getSyncREM(Syncable.Types.Homework).isEmpty() &&
            loc.getSyncREM(Syncable.Types.Test).isEmpty() &&
            loc.getSyncREM(Syncable.Types.WebPage).isEmpty() &&
            loc.getSyncREM(Syncable.Types.Vocabulary).isEmpty()
        ) {
            return
        }

        val listIDhomework = loc.getSync(Syncable.Types.Homework)
        val listIDweb = loc.getSync(Syncable.Types.WebPage)
        val listIDword = loc.getSync(Syncable.Types.Vocabulary)
        val listIDtest = loc.getSync(Syncable.Types.Test)

        val listIDhomeworkREM = loc.getSyncREM(Syncable.Types.Homework)
        val listIDwebREM = loc.getSyncREM(Syncable.Types.WebPage)
        val listIDwordREM = loc.getSyncREM(Syncable.Types.Vocabulary)
        val listIDtestREM = loc.getSyncREM(Syncable.Types.Test)

        val listHomework = mutableListOf<HomeWork>()
        val listWeb = mutableListOf<Web>()
        val listTest = mutableListOf<Test>()
        val listWord = mutableListOf<Word>()
        val toRemove = mutableListOf<Syncable>()
        val toRemoveWords = mutableListOf<String>()

        for (id in listIDhomework) {
            student.getHomework(id)?.let {
                listHomework += it
            }
        }
        for (id in listIDweb) {
            student.getWeb(id)?.let {
                listWeb += it
            }
        }
        for (id in listIDtest) {
            student.getTest(id)?.let {
                listTest += it
            }
        }
        for (id in listIDword) {
            student.getWord(id)?.let {
                listWord += it
            }
        }

        for (id in listIDhomeworkREM) {
            toRemove += HomeWork(id, "", Date(), mutableListOf(), Date(), "", "")
        }
        for (id in listIDwebREM) {
            toRemove += Web(id, "", "", "")
        }
        for (id in listIDwordREM) {
            toRemoveWords += id
        }

        val studentSync = Student(
            student.id,
            student.name,
            listWord,
            listWeb,
            listHomework,
            student.class_id,
            false,
            listTest
        )

        if (async) {
            CoroutineScope(Dispatchers.IO).launch {
                syncStudentSubProcess(studentSync, toRemove, toRemoveWords)
            }
        } else {
            runBlocking {
                syncStudentSubProcess(studentSync, toRemove, toRemoveWords)
            }
        }
    }

    /**
     * Saves [student] firstly to local database, than tries to synchronize data, that failed to
     * synchronize on last run of application, and then tries to synchronize new changes in data
     * during current run of application
     *
     * [local] determines if student should be save only to local database
     */
    fun saveStudent(student: Student, local: Boolean = false) {
        if (!loggedIn) throw DatabaseException("you're not log in")

        if (student.isToSyncLocal()) { //saving to local database
            try {
                loc.saveStudent(listOf<Student>(student))

                val homework =  student.localSync()[StudentSync.Actions.Add]!!.filter {
                    it.getType() == Syncable.Types.Homework
                }.map {
                    it as HomeWork
                }
                val homeworkMap = mutableMapOf<HomeWork, Student>()
                for(h in homework) homeworkMap[h] = student
                loc.saveHomework(homeworkMap)

                loc.saveWeb(
                    student.localSync()[StudentSync.Actions.Add]!!.filter {
                        it.getType() == Syncable.Types.WebPage
                    }.map {
                        it as Web
                    }
                )
                loc.saveTest(
                    student.localSync()[StudentSync.Actions.Add]!!.filter {
                        it.getType() == Syncable.Types.Test
                    }.map {
                        it as Test
                    }
                )
                loc.saveWords(
                    student.localSync()[StudentSync.Actions.Add]!!.filter {
                        it.getType() == Syncable.Types.Vocabulary
                    }.map {
                        it as Word
                    }
                )

                loc.deleteObj(student.localSync()[StudentSync.Actions.Remove]!!)

                student.localSyncDel()
            } catch (e: SQLiteException) {
                println("local db error (save_student) " + e.localizedMessage)
            }
        }

        if (!local) { //synchronization of changes from past
            syncStudent(student)
        }

        if (!local && student.isToSyncRemote()) { //synchronization of currently pending changes
            val listHomework = mutableListOf<HomeWork>()
            val listWeb = mutableListOf<Web>()
            val listWord = mutableListOf<Word>()
            val listTest = mutableListOf<Test>()

            for (o in student.remoteSync()[StudentSync.Actions.Add]!!) {
                when (o.getType()) {
                    Syncable.Types.Homework -> listHomework += o as HomeWork
                    Syncable.Types.WebPage -> listWeb += o as Web
                    Syncable.Types.Vocabulary -> listWord += o as Word
                    Syncable.Types.Test -> listTest += o as Test
                }
            }
            val studentSync = Student(
                student.id,
                student.name,
                listWord,
                listWeb,
                listHomework,
                student.class_id,
                false,
                listTest
            )
            val toRemoveObj = mutableListOf<Syncable>()
            for (obj in student.remoteSync()[StudentSync.Actions.Remove]!!) {
                if (obj.getType() != Syncable.Types.Vocabulary) toRemoveObj.add(obj)
            }

            val wordsToRemove = mutableListOf<String>()
            val wordsToRemoveObj = mutableListOf<Syncable>()
            for (w in student.remoteSync()[StudentSync.Actions.Remove]!!) {
                if (w.getType() == Syncable.Types.Vocabulary) {
                    wordsToRemove += VocabularyUtils.addList(w as Word)
                    wordsToRemoveObj += w
                }
            }

            CoroutineScope(Dispatchers.IO).launch {
                rem?.let {
                    try {
                        if (!it.saveStudent(studentSync).succes) {
                            val toSave = mutableListOf<Syncable>()
                            toSave += listWord
                            toSave += listWeb
                            toSave += listHomework
                            toSave += listTest

                            loc.saveSync(toSave) //if synchronization fails, it saves to local database
                                                //which objects need to be synchronized later
                        }
                        if (toRemoveObj.count() != 0 && !it.remove(toRemoveObj).succes
                        ) {
                            loc.saveSyncRem(toRemoveObj) //removal of objects and words is done
                                                        //by separate message in REST API
                        }
                        if (wordsToRemove.count() != 0 &&
                            !it.removeWords(wordsToRemove).succes
                        ) {
                            loc.saveSyncRem(wordsToRemoveObj)
                        }
                    } catch (e: HttpConnector.HttpConnector_exception) {
                        println("remote db error (save_student) " + e.localizedMessage)

                        val toSave = mutableListOf<Syncable>()
                        toSave += listWord
                        toSave += listWeb
                        toSave += listHomework
                        toSave += listTest
                        loc.saveSync(toSave)
                        loc.saveSyncRem(toRemoveObj)
                        loc.saveSyncRem(wordsToRemoveObj)
                    }
                }

                student.remoteSyncDel()
                if (rem == null) throw DatabaseException("internal error")
            }
        }
    }

    /**
     * Assigns new homework to class, this method is method of Database and not a teacher class, see
     * Teacher class
     *
     * @return returns currently logged teacher on success or null
     */
    fun saveHomework(homework: HomeWork): Teacher? {
        if (!loggedIn) throw DatabaseException("you're not log in")
        if (!teacherPriv) throw DatabaseException("you don't have privileges for this action")

        val teacherLoc = getTeacherloc() ?: throw Exception("internal error - teacher reference null")
        val homeworkMap = mutableMapOf<HomeWork, Student>()

        try {
            val homeworkToSave = mutableListOf<HomeWork>()
            val studentsInClass = teacherLoc.getClass(homework.class_id)!!

            for(s in studentsInClass.students){
                val homeworkCopy = homework.copy(id= Tools.getUUID())
                homeworkToSave.add(homeworkCopy)
                homeworkMap[homeworkCopy] = s
            }

            loc.saveHomework(homeworkMap)
        } catch (e: SQLiteException) {
            println("local db error (save_homework) " + e.localizedMessage)
            return null
        }

        CoroutineScope(Dispatchers.IO).launch {
            rem?.let {
                try {
                    if (!it.addHomework(homeworkMap).succes) {
                        loc.saveSync(homeworkMap.keys.toList())
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    println("remote db error (save_homework) " + e.localizedMessage)
                    loc.saveSync(homeworkMap.keys.toList())
                }
            }

            if (rem == null) throw DatabaseException("internal error")
        }

        return getTeacherloc()
    }

    /**
     * Assigns new words to class, this method is method of Database and not a teacher class, see
     * Teacher class
     *
     * @return returns currently logged teacher on success or null
     */
    fun addWords(list: List<Word>): Teacher? {
        if (!loggedIn) throw DatabaseException("you're not log in")
        if (!teacherPriv) throw DatabaseException("you don't have priviliges for this action")

        try {
            loc.saveWords(list)
        } catch (e: SQLiteException) {
            println("local db error (save_words) " + e.localizedMessage)
        }

        CoroutineScope(Dispatchers.IO).launch {
            rem?.let {
                try {
                    if (!it.addWords(list).succes) {
                        loc.saveSync(list)
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    println("remote db error (save_words) " + e.localizedMessage)
                    loc.saveSync(list)
                }
            }

            if (rem == null) throw DatabaseException("internal error")
        }

        return getTeacherloc()
    }

    /**
     * Delete homework for specified class, this method is method of Database and not a teacher class, see
     * Teacher class
     *
     * @return returns currently logged teacher on success or null
     */
    fun removeHomework(homework: HomeWork): Teacher? {
        if (!loggedIn) throw DatabaseException("you're not log in")
        if (!teacherPriv) throw DatabaseException("you don't have priviliges for this action")

        try {
            loc.deleteObj(listOf(homework))
        } catch (e: SQLiteException) {
            println("local db error (save_words) " + e.localizedMessage)
        }

        CoroutineScope(Dispatchers.IO).launch {
            rem?.let {
                try {
                    if (!it.remove(homework).succes) {
                        loc.saveSyncRem(listOf(homework))
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    println("remote db error (save_words) " + e.localizedMessage)
                    loc.saveSyncRem(listOf(homework))
                }
            }

            if (rem == null) throw DatabaseException("internal error")
        }

        return getTeacherloc()
    }

    /**
     * Delete specified words for class, this method is method of Database and not a teacher class, see
     * Teacher class
     *
     * @return returns currently logged teacher on success or null
     */
    fun removeWords(list: List<Word>): Teacher? {
        if (!loggedIn) throw DatabaseException("you're not log in")
        if (!teacherPriv) throw DatabaseException("you don't have priviliges for this action")

        try {
            loc.deleteObj(list)
        } catch (e: SQLiteException) {
            println("local db error (save_words) " + e.localizedMessage)
        }

        val wordsToRemove = mutableListOf<String>()
        for (word in list) {
            wordsToRemove.add(VocabularyUtils.addList(word))
        }

        CoroutineScope(Dispatchers.IO).launch {
            rem?.let {
                try {
                    if (!it.removeWords(wordsToRemove).succes) {
                        loc.saveSyncRem(list)
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    println("remote db error (save_words) " + e.localizedMessage)
                    loc.saveSyncRem(list)
                }
            }

            if (rem == null) throw DatabaseException("internal error")
        }

        return getTeacherloc()
    }

    /**
     * Function to push new updates about homework and words for classes to remote database
     */
    private fun syncTeacher(teacher: Teacher) {
        if (loc.getSync(Syncable.Types.Homework).isNotEmpty()) {
            val homeworkID = loc.getSync(Syncable.Types.Homework)
            val homeworkMap = mutableMapOf<HomeWork, Student>()

            for (c in teacher.classes) {
                for(s in c.students){
                    for(id in homeworkID){
                        (s.getHomework(id))?.let{
                            homeworkMap[it] = s
                        }
                    }
                }
            }

            runBlocking {
                rem?.let{
                    try{
                        if(it.addHomework(homeworkMap).succes){
                            loc.deleteSync(Syncable.Types.Homework)
                        }
                    } catch(e: HttpConnector.HttpConnector_exception){
                        println("remote db error (sync_teacher) " + e.localizedMessage)
                    }
                }
            }
        }

        if (loc.getSync(Syncable.Types.Vocabulary).isNotEmpty()) {
            var words = listOf<Word>()
            for (id in loc.getSync(Syncable.Types.Vocabulary)) {
                for (c in teacher.classes) {
                    c.students[0].getWord(id)?.let {
                        words += it
                    }
                }
            }

            runBlocking {
                rem?.let {
                    try {
                        if (it.addWords(words).succes) {
                            loc.deleteSync(Syncable.Types.Vocabulary)
                        }
                    } catch (e: HttpConnector.HttpConnector_exception) {
                        println("remote db error (sync_teacher) " + e.localizedMessage)
                    }
                }

                if (rem == null) throw DatabaseException("internal error")
            }
        }

        if (loc.getSyncREM(Syncable.Types.Vocabulary).isNotEmpty()) {
            val wordsToRemove = loc.getSyncREM(Syncable.Types.Vocabulary)

            runBlocking {
                rem?.let {
                    try {
                        if (it.removeWords(wordsToRemove).succes) {
                            loc.deleteSyncREM(Syncable.Types.Vocabulary)
                        }
                    } catch (e: HttpConnector.HttpConnector_exception) {
                        println("remote db error (sync_teacher) " + e.localizedMessage)
                    }
                }

                if (rem == null) throw DatabaseException("internal error")
            }
        }

        if (loc.getSyncREM(Syncable.Types.Homework).isNotEmpty()) {
            val homeworkToRemove = mutableListOf<HomeWork>()
            for (id in loc.getSyncREM(Syncable.Types.Homework)) {
                homeworkToRemove.add(HomeWork("", "", Date(), listOf(), Date(), "", id))
            }

            runBlocking {
                rem?.let {
                    try {
                        if (it.remove(homeworkToRemove).succes) {
                            loc.deleteSyncREM(Syncable.Types.Homework)
                        }
                    } catch (e: HttpConnector.HttpConnector_exception) {
                        println("remote db error (sync_teacher) " + e.localizedMessage)
                    }
                }
            }

            if (rem == null) throw DatabaseException("internal error")
        }
    }

    /**
     * returns list of students for [classId]
     */
    private fun getStudentLocList(classId: String): List<Student>? {
        try {
            val toReturn = mutableListOf<Student>()

            for (st in loc.getStudent(classId, true)) {
                val id = st.id
                val homework = loc.getHomework(classId, id)
                val webPages = loc.getWeb(id)
                val tests = loc.getTest(id)
                val words = loc.getWords(classId)

                toReturn.add(Student(id, st.name, words, webPages, homework, classId, false, tests))
            }

            return toReturn
        } catch (e: SQLException) {
            println("local db error (get_student) " + e.localizedMessage)
        }

        return null
    }

    /**
     * loads currently logged student from local database
     */
    private fun getStudentLoc(): Student? {
        id?.let {
            return getStudentLoc(it)
        }

        return null
    }

    /**
     * loads student based on [id]
     */
    private fun getStudentLoc(id: String): Student? {
        try {
            val st_list = loc.getStudent(id)
            if (st_list.count() == 0) return null

            val st = st_list[0]
            val id = st.id
            val homework = loc.getHomework(st.class_id)
            val webPages = loc.getWeb(id)
            val tests = loc.getTest(id)
            val words = loc.getWords(id)
            words.addAll(loc.getWords(st.class_id))

            return Student(id, st.name, words, webPages, homework, st.class_id, false, tests)
        } catch (e: SQLException) {
            println("local db error (get_student) " + e.localizedMessage)
        }

        return null
    }

    /**
     * loads currently logged student firstly by trying get newest data from remote database,
     * and than by loading it from local database, but if any changes are pending then, they are pushed
     * first
     */
    fun getStudent(): Student? {
        if (!loggedIn) throw DatabaseException("you're not log in")

        var studentLoc = getStudentLoc()
        studentLoc?.let {
            syncStudent(it, false) //pushes changes to remote database
        }

        var student: Student? = null
        runBlocking {
            rem?.let {
                val remDB = it
                try {
                    id?.let {
                        student = remDB.getStudent(it)
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    println("remote db error (get_student) " + e.localizedMessage)
                }
            }

            if (rem == null) throw DatabaseException("internal error")
        }

        student?.let { //saves new data retrieved from remote database
            loc.deleteTable(
                Syncable.Types.Homework,
                Syncable.Types.Test,
                Syncable.Types.Vocabulary,
                Syncable.Types.WebPage,
                Syncable.Types.Student
            )
            saveStudent(it, true)

            it.remoteSyncDel()
            return it
        }

        return studentLoc
    }

    /**
     *loads currently logged teacher from local database
     */
    private fun getTeacherloc(): Teacher? {
        id?.let {
            return getTeacherloc(it)
        }

        return null
    }

    /**
     * loads teacher based on [id] from local database
     */
    private fun getTeacherloc(id: String): Teacher? {
        try {
            val teacher = loc.getTeacher(id)
            val classes = mutableListOf<Class>()

            for (c in teacher?.classes ?: listOf<Class>()) {
                val students = getStudentLocList(c.id)
                    ?: throw DatabaseException("inconsistent data number of students in class is 0")
                classes += Class(c.id, students)
            }

            teacher?.let {
                if (classes.count() != 0) {
                    return Teacher(teacher.id, teacher.name, classes)
                }
            }
        } catch (e: SQLException) {
            println("local db error (get_teacher) " + e.localizedMessage)
        }

        return null
    }

    /**
     * loads currently logged teacher firstly by trying get newest data from remote database,
     * and than by loading it from local database, but if any changes are pending then, they are pushed
     * first
     */
    fun getTeacher(): Teacher? {
        if (!loggedIn) throw DatabaseException("you're not log in")

        var teacherLoc: Teacher? = getTeacherloc()
        var teacher: Teacher? = null

        teacherLoc?.let {
            syncTeacher(it)
        }

        runBlocking {
            rem?.let {
                try {
                    val remDB = it
                    id?.let {
                        teacher = remDB.getTeacher(it)
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    println("remote db error (get_teacher) " + e.localizedMessage)
                }
            }
        }

        teacher?.let {
            try {
                loc.deleteTable(
                    Syncable.Types.Student,
                    Syncable.Types.Teacher,
                    Syncable.Types.Homework,
                    Syncable.Types.Test,
                    Syncable.Types.Vocabulary,
                    Syncable.Types.WebPage
                )
                for (c in it.classes) {
                    for (s in c.students) {
                        saveStudent(s, true)
                        s.remoteSyncDel()
                    }
                }
                loc.saveTeacher(it)

                return it
            } catch (e: SQLException) {
                println("local db error (get_teacher) " + e.localizedMessage)
            }
        }

        return teacherLoc
    }

    /**
     * loads rank list of students, and returns data via call back function [onResult]
     */
    fun getRankList(onResult: (result: Map<String,Pair<Int,Int>>?)->Unit){
        if (!loggedIn) throw DatabaseException("you're not log in")

        CoroutineScope(Dispatchers.IO).launch {
            rem?.let {
                try {
                    val remDB = it
                    id?.let {
                         onResult(remDB.getRankList())
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    onResult(null)
                    println("remote db error (get_teacher) " + e.localizedMessage)
                }
            } ?: onResult(null)
        }
    }

    /**
     * saves settings in format of key to value to shared preferences
     */
    fun saveSetting(data: Map<String, Any>) {
        val pref = activity.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE) ?: return
        with(pref.edit()) {
            for ((key, value) in data) {
                when (value) {
                    is Int -> putInt(key, value)
                    is String -> putString(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                }
            }
            apply()
        }
    }

    /**
     * delete settings based on [key]
     */
    private fun removeSetting(key: String) {
        val pref = activity.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE) ?: return
        with(pref.edit()) {
            remove(key)
            apply()
        }
    }

    /**
     * read settings based on [key]
     */
    fun getSetting(key: String): Any? {
        val pref = activity.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE) ?: return null
        return pref.all[key]
    }

    /**
     * log in new user and tries to verify if login credentials are valid if internet connection
     * is available
     *
     * @return information if login was success and if user is teacher
     */
    fun login(): Map<String, Boolean> {
        val login = getSetting(Settings.login)
        val pass = getSetting(Settings.password)
        val teacher = getSetting(Settings.teacher)

        var confirm = mapOf<String, Boolean>(
            DBloginMessage.succes to (login != null && pass != null && teacher != null),
            DBloginMessage.isTeacher to (teacher as? Boolean ?: false),
            DBloginMessage.connectionError to false
        )

        if (login == null || pass == null) {
            if (id == null || hash == null) throw DatabaseException("wrong initialization")

            runBlocking {
                try {
                    val remCr = rem!!.auth(id!!, hash!!)
                    if (remCr[DBloginMessage.succes] == true) {
                        loc.deleteTable(
                            Syncable.Types.Student,
                            Syncable.Types.Test,
                            Syncable.Types.Teacher,
                            Syncable.Types.Homework,
                            Syncable.Types.WebPage,
                            Syncable.Types.Vocabulary
                        )
                        loc.deleteSync(
                            Syncable.Types.Student,
                            Syncable.Types.Test,
                            Syncable.Types.Teacher,
                            Syncable.Types.Homework,
                            Syncable.Types.WebPage,
                            Syncable.Types.Vocabulary
                        )
                        loc.deleteSyncREM(
                            Syncable.Types.Student,
                            Syncable.Types.Test,
                            Syncable.Types.Teacher,
                            Syncable.Types.Homework,
                            Syncable.Types.WebPage,
                            Syncable.Types.Vocabulary
                        )

                        saveSetting(
                            mapOf(
                                Settings.login to id!!,
                                Settings.password to hash!!,
                                Settings.teacher to (remCr[DBloginMessage.isTeacher] ?: false)
                            )
                        )

                        confirm = mapOf<String, Boolean>(
                            DBloginMessage.succes to true,
                            DBloginMessage.isTeacher to (remCr[DBloginMessage.isTeacher] ?: false)
                        )
                    }
                } catch (e: HttpConnector.HttpConnector_exception) {
                    confirm = mapOf<String, Boolean>(
                        DBloginMessage.succes to false,
                        DBloginMessage.isTeacher to false,
                        DBloginMessage.connectionError to true
                    )

                    println("remote db error (login) " + e.localizedMessage)
                }
            }
        } else {
            id = login as String
            hash = pass as String

            runBlocking {
                try {
                    rem = DB_rem(id!!, hash!!, context.contentResolver)

                    confirm = rem!!.auth(id!!, hash!!)
                } catch (e: HttpConnector.HttpConnector_exception) {
                    confirm = mapOf<String, Boolean>(
                        DBloginMessage.succes to confirm[DBloginMessage.succes]!!,
                        DBloginMessage.isTeacher to confirm[DBloginMessage.isTeacher]!!,
                        DBloginMessage.connectionError to true
                    )
                    println("remote db error (login) " + e.localizedMessage)
                }
            }
        }

        if (confirm[DBloginMessage.succes] == true) {
            loggedIn = true
        }
        if (confirm[DBloginMessage.isTeacher] == true) {
            teacherPriv = true
        }
        return confirm
    }

    /**
     * logout user from application
     */
    fun logout() {
        removeSetting(Settings.teacher)
        removeSetting(Settings.login)
        removeSetting(Settings.password)
    }
}