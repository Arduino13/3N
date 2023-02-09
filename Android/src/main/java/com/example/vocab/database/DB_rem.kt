package com.example.vocab.database

import android.content.ContentResolver
import com.example.vocab.*
import com.example.vocab.basic.*
import java.lang.Exception
import kotlin.collections.mutableMapOf

/**
 * Manages requests for remote database, using JSON
 *
 * @property server url address for remote database
 * @property content used by HttpConnector
 * @property userId to log in to remote database
 * @property userHash to log in to remote database
 */
class DB_rem(user_idF: String, user_hashF: String, contentF: ContentResolver) {
    class DBmessage(msg: String? = null){ //class to communicate database status with Database class
        enum class msg_type(val context: String){
            successStudent("success/student"),
            successVocabulary("success/vocabulary"),
            sucessHomework("success/homework"),
            successTeacher("success/teacher"),
            errorStudent("database_error/student"),
            errorVocabulary("database_error/vocabulary"),
            errorHomework("database_error/homework"),
            errorTeacher("database_error/teacher"),
            errorDB("database error"),
            errorParser("parsing error"),
            update("upd"),
            error("error"),
            none("")
        }

        companion object{
            private val map = msg_type.values().associateBy(msg_type::context)
            fun fromString(type: String) = map[type] ?: throw Exception()
        } //reverse lookup table

        var value = msg_type.none
            private set

        val succes: Boolean
            get(){
                return value == msg_type.successStudent ||
                        value == msg_type.sucessHomework ||
                        value == msg_type.successVocabulary ||
                        value == msg_type.successTeacher
            }

        init{
            value = msg?.let{
                try {
                    fromString(msg)
                } catch (e: Exception) {
                    msg_type.error
                }
            } ?: msg_type.none
        }
    }

    private val server = "https://172.16.0.3:8080/run"
    private val content = contentF
    private var userId = user_idF
    private var userHash = user_hashF

    /**
     * parse students from dictionary [data]
     */
    private fun processStudents(data: HttpConnector.Return?): List<Student>{
        val toReturn = mutableListOf<Student>()
        val wordsLoaded = mutableListOf<Word>()

        if(data != null) {
            data.sections["content"]?.sections?.let {
                for ((id_st, data) in it){
                    val homeTemp = mutableListOf<HomeWork>()
                    val webTemp = mutableListOf<Web>()
                    val wordTemp = mutableListOf<Word>()

                    for ((classId, value) in data.sections["words"]?.sections ?: mapOf<String, HttpConnector.Return>()) { //loads words
                        for ((from, to) in value.values) {
                            var word = Word(
                                Tools.getUUID(),
                                VocabularyUtils.separateWord(from),
                                to,
                                VocabularyUtils.separateList(from),
                                class_id = classId
                            )

                            if(wordsLoaded.containsFrom(word)) word = word.copy(id = wordsLoaded.indexFrom(word)!!.id)

                            wordTemp += word
                        }

                        wordsLoaded.addAll(wordTemp)
                    }

                    val name = data.values["name"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))
                    val classId = data.values["class_id"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))

                    data.sections["homework"]?.let { //loading homework
                        for ((id, data) in it.sections) {
                            val name = data.values["name"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))
                            val date = DateUtils.fromString(data.values["date"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data")))
                            val dateC = DateUtils.fromString(data.values["date_completed"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data")))
                            val completed = data.values["completed"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))
                            val words = data.sections["words"]?.values ?: mapOf<String, String>()
                            val internalId = data.values["homework_id"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))
                            val completedWords = mutableListOf<Word>()
                            val wordsObj = mutableListOf<Word>()
                            for ((from, to) in words) {
                                var word = Word(Tools.getUUID(),
                                    VocabularyUtils.separateWord(from),
                                    VocabularyUtils.separateWord(to),
                                    VocabularyUtils.separateList(from))
                                if(wordsLoaded.containsFrom(word)) word = word.copy(id = wordsLoaded.indexFrom(word)!!.id)

                                wordsObj.add(word)
                            }

                            if (data.values["words_completed"]?.count() != 0) {
                                val tempDict: List<String> = data.values["words_completed"]?.split(",") ?: listOf<String>()
                                for (wordS in tempDict) {
                                    var word = Word(Tools.getUUID(),
                                        VocabularyUtils.separateWord(wordS),
                                        "",
                                        VocabularyUtils.separateList(wordS))
                                    if(wordsObj.containsFromList(word))
                                        completedWords.add(wordsObj.indexFromList(word)!!)
                                }
                            }

                            val problematicWords = mutableListOf<Word>()
                            for(word in data.values["problematicWords"]?.split(",") ?: listOf<String>()){
                                if(wordsObj.containsFromList(Word(Tools.getUUID(),
                                        VocabularyUtils.separateWord(word))))
                                     problematicWords.add(wordsObj.indexFromList(Word(Tools.getUUID(),word))!!)
                            }

                            homeTemp.add(
                                HomeWork(
                                    id, name, date, wordsObj, dateC, classId, internalId,
                                    completed == "true", completedWords, problematicWords)
                            )
                        }
                    }

                    data.sections["web_pages"]?.let { //loading webs
                        for ((id, data) in it.sections) {
                            val name = data.values["name"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))
                            val address = data.values["address"] ?: throw(HttpConnector.HttpConnector_exception("inconsistent data"))
                            webTemp.add(Web(id, address, name, id_st))
                        }
                    }

                    toReturn.add(Student(id_st,name,wordTemp,webTemp,homeTemp,classId,true))
                }
            } ?: throw HttpConnector.HttpConnector_exception("inconsistent data")
        }
        else{
            throw HttpConnector.HttpConnector_exception("inconsistent data")
        }

        return toReturn
    }

    /**
     * sends request to load student, and then tries to parse student's data
     */
    suspend fun getStudent(id: String): Student {
        val connection = HttpConnector(userId, userHash, content)

        connection.openMessage(server, "GET")
        connection.addMessage(
            mapOf<String, String>(
                "TYPE" to Syncable.Types.Student.type,
                "ID" to id
            )
        )

        val data = connection.sendResponse()

        return processStudents(data).first()
    }

    /**
     * loads students based on [classId]
     */
    suspend fun getStudents(classId: String): List<Student>{
        val connection = HttpConnector(userId, userHash, content)

        connection.openMessage(server,"GET")
        connection.addMessage(
            mapOf<String, String>(
                "TYPE" to Syncable.Types.Student.type,
                "CLASS_ID" to classId
            )
        )

        val data = connection.sendResponse()

        return processStudents(data)
    }

    /**
     * request for teacher's data and then process them
     */
    suspend fun getTeacher(id: String): Teacher{
        val connection = HttpConnector(userId,userHash,content)

        connection.openMessage(server,"GET")
        connection.addMessage(mapOf<String,String>(
            "TYPE" to Syncable.Types.Teacher.type,
            "ID" to id
        ))

        val data = connection.sendResponse()
        if(data != null){
            data.sections["content"]?.sections?.let{
                for((id,data) in it){
                    val classes = data.values["classes"]?.split(",") ?: throw HttpConnector.HttpConnector_exception("inconsistent data")
                    val name = data.values["name"] ?: throw HttpConnector.HttpConnector_exception("inconsistent data")

                    val classesObj = mutableListOf<Class>()
                    for(cls in classes){
                        classesObj.add(
                            Class(cls,getStudents(cls))
                        )
                    }

                    return Teacher(id,name,classesObj)
                }
                throw HttpConnector.HttpConnector_exception("inconsistent data")

            } ?: throw HttpConnector.HttpConnector_exception("inconsistent data")
        }
        else{
            throw HttpConnector.HttpConnector_exception("inconsistent data")
        }
    }

    /**
     * rank list of students
     */
    suspend fun getRankList(): Map<String, Pair<Int, Int>>{
        val connection = HttpConnector(userId, userHash, content)

        connection.openMessage(server, "GET")
        connection.addMessage(mapOf<String,String>(
            "TYPE" to "rankList"
        ))

        val data = connection.sendResponse()
        val toReturn = mutableMapOf<String, Pair<Int, Int>>()

        data?.sections?.get("content")?.sections?.let{
            for((id,data) in it){
                    toReturn[id] = Pair(
                        (data.values["number_of_tests"] ?: throw HttpConnector.HttpConnector_exception("inconsistent data")).toInt(),
                        (data.values["success_ratio"] ?: throw HttpConnector.HttpConnector_exception("inconsistent data")).toInt()
                    )
            }
        }

        return toReturn
    }

    /**
     * saves student
     *
     * @return DBmessage object
     */
    suspend fun saveStudent(student: Student): DBmessage {
        val connection = HttpConnector(userId,userHash,content)

        val homework = mutableMapOf<String, MutableMap<String, MutableMap<String, Any>>>()
        val web = mutableMapOf<String, MutableMap<String, MutableMap<String, Any>>>()
        val words = mutableMapOf<String, MutableMap<String, String>>()
        val tests = mutableMapOf<String, MutableMap<String, MutableMap<String, Any>>>()

        var temp = mutableMapOf<String, MutableMap<String, Any>>()

        for(home in student.listHomework) {
            val words = home.completedWords.joinToString(";") { VocabularyUtils.addList(it) }
            temp[home.id] = mutableMapOf<String, Any>(
                    "completed" to home.completed.toString(),
                    "words_completed" to words
            )
        }
        homework["homework"] = temp

        temp = mutableMapOf()
        for(page in student.listWebs){
            temp[page.id] = mutableMapOf<String,Any>(
                "address" to page.address,
                "name" to page.name
            )
        }
        web["web_pages"] = temp

        temp = mutableMapOf()
        for(test in student.listTests){
            val wrongWords = test.wrongWords.joinToString(";") { it.from }
            val words = mutableMapOf<String, String>()
            for(word in test.words){
                words[VocabularyUtils.addList(word)] = word.to
            }

            temp[test.id] = mutableMapOf<String,Any>(
                "date" to DateUtils.fromDate2String(test.date),
                "words" to words,
                "wrong_words" to wrongWords,
                "homework_id" to (test.homeworkID ?: "")
            )
        }
        tests["tests"] = temp

        words["words"] = mutableMapOf<String,String>()
        for(w in student.listWords){
            words.merge("words",mutableMapOf<String,String>(
                VocabularyUtils.addList(w) to w.to
            ))
        }

        connection.openData(server)
        connection.addData(
            arrayOf<Map<String,Any>>(
                mapOf<String,String>(
                    "id" to student.id,
                    "name" to student.name,
                    "class_id" to student.class_id
                ),
                homework,
                web,
                tests,
                words
            )
        )

        return DBmessage(
            connection.sendResponse()?.sections?.get("content")?.values?.get("text")
        )
    }

    /**
     * Send request to delete object [obj]
     */
    suspend fun remove(obj: Syncable): DBmessage{
        val connection = HttpConnector(userId,userHash,content)

        connection.openMessage(server,"DEL")
        when(obj.getType()){
            Syncable.Types.Homework -> connection.addMessage(
                mapOf<String,String>(
                    "TYPE" to obj.getType().type,
                    "HOMEWORK_ID" to (obj as HomeWork).homework_id
                )
            )
            else -> connection.addMessage(
                mapOf<String,String>(
                    "TYPE" to obj.getType().type,
                    "ID" to obj.getID()
                )
            )
        }

        return DBmessage(
            connection.sendResponse()?.sections?.get("content")?.values?.get("text")
        )
    }

    /**
     * Send request to delete objects [objs]
     */
    suspend fun remove(objs: List<Syncable>): DBmessage{
        var toReturn = DBmessage()
        for(obj in objs){
            val returnValue = remove(obj)
            if(returnValue.succes && toReturn.succes) toReturn = returnValue
            else toReturn = DBmessage(DBmessage.msg_type.error.context)
        }

        return toReturn
    }

    /**
     * Send request to delete words [words]
     */
    suspend fun removeWords(words: List<String>): DBmessage{
        val connection = HttpConnector(userId,userHash,content)

        connection.openMessage(server,"DEL")
        val wordsA = words.joinToString(";")
        connection.addMessage(
            mapOf<String,String>(
                "TYPE" to "vocabulary",
                "FROM_v" to wordsA
            )
        )

        return DBmessage(
            connection.sendResponse()?.sections?.get("content")?.values?.get("text")
        )
    }

    /*suspend fun removeWords(id: String): DBmessage{
        val connection = HttpConnector(userId,userHash,content)

        connection.openMessage(server,"DEL")
        connection.addMessage(
            mapOf<String,String>(
                "TYPE" to "vocabulary",
                "CLASS_ID" to id
            )
        )

        return DBmessage(
            connection.sendResponse()?.sections?.get("content")?.values?.get("text")
        )
    }*/

    /**
     * send request to add new homework assigned by teacher
     */
    suspend fun addHomework(homework: Map<HomeWork, Student>): DBmessage{
        val connection = HttpConnector(userId,userHash,content)

        val homeworkMap = mutableMapOf<String, Map<String, Any>>()
        for((h,s) in homework){
            val words = mutableMapOf<String,String>()
            for(w in h.wordList){
                words[VocabularyUtils.addList(w)] = w.to
            }

            homeworkMap[h.id] =
                mapOf(
                    "date" to DateUtils.fromDate2String(h.date),
                    "date_completed" to DateUtils.fromDate2String(h.dateCompleted),
                    "class_id" to h.class_id,
                    "name" to h.name,
                    "homework_id" to h.homework_id,
                    "student_id" to s.id,
                    "words" to words
                )
        }

        connection.openData(server)
        connection.addData(arrayOf<Map<String,Any>>(
            mapOf(
                "homework" to homeworkMap
            )
        ))

        return DBmessage(
            connection.sendResponse()?.sections?.get("content")?.values?.get("text")
        )
    }

    /**
     * send request to add new words assigned by teacher
     */
    suspend fun addWords(list: List<Word>): DBmessage{
        val connection = HttpConnector(userId,userHash,content)
        connection.openData(server)

        val words = mutableMapOf<String,String>()
        var classId = ""
        for(w in list){
            classId = w.class_id
            words[VocabularyUtils.addList(w)] = w.to
        }

        connection.addData(arrayOf<Map<String,Any>>(
            mapOf<String, Map<String,Map<String,String>>>(
               "words" to mapOf(
                    classId to words
                )
            )
        ))

        return DBmessage(
            connection.sendResponse()?.sections?.get("content")?.values?.get("text")
        )
    }

    /**
     * Use to log in to application and for validating if credentials are still valid later on
     *
     * @return returns dictionary if credentials are valid and if user is teacher
     */
    suspend fun auth(id: String, passwdHash: String): Map<String,Boolean>{
        val connection = HttpConnector(userId,userHash,content)

        connection.openMessage(server, "AUTH")
        connection.addMessage(
            mapOf<String, String>(
                "ID" to id,
                "HASH" to passwdHash
            )
        )

        val data = connection.sendResponse()
        when {
            data?.sections?.get("content")?.values?.get("text") ==  DBmessage.msg_type.successStudent.context -> {
                return mapOf<String, Boolean>(
                    "auth" to true,
                    "teacher" to false
                )
            }
            data?.sections?.get("content")?.values?.get("text") == DBmessage.msg_type.successTeacher.context -> {
                return mapOf<String, Boolean>(
                    "auth" to true,
                    "teacher" to true
                )
            }
            else -> {
                return mapOf<String, Boolean>(
                    "auth" to false,
                    "teacher" to false
                )
            }
        }
    }
}