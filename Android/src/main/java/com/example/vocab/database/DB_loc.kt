package com.example.vocab.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.vocab.DateUtils
import com.example.vocab.VocabularyUtils
import com.example.vocab.basic.*
import com.example.vocab.containsFromList
import com.example.vocab.indexFromList

/**
 * Manages local database
 *
 * @property context to access application resources
 */
class DB_loc(private val context: Context) {
    private class DB_loc_helper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        object dbNamesapce {
            object HomeWorkDB : BaseColumns {
                const val TABLE_NAME = "homework"
                const val ID = "id"
                const val NAME = "name"
                const val DATECOMPLETED = "dateCompleted"
                const val DATE = "date"
                const val CLASSID = "class_id"
                const val INTERNALID = "internal_id"
                const val STUDENT_ID = "student_id"
                const val COMPLETED = "completed"
                const val WORDLIST = "wordList"
                const val COMPLETEDWORDS = "completedWords"
                const val PROBLEMATICWORDS = "problematicWords"
                val _PROJECTION = arrayOf(ID,NAME,DATE,DATECOMPLETED,CLASSID,INTERNALID,STUDENT_ID,COMPLETED,WORDLIST,COMPLETEDWORDS, PROBLEMATICWORDS)
            }

            object StudentDB : BaseColumns {
                const val TABLE_NAME = "students"
                const val ID = "id"
                const val CLASSID = "class_id"
                const val NAME = "name"
                val _PROJECTION = arrayOf(ID,CLASSID,NAME)
            }

            object TeacherDB : BaseColumns {
                const val TABLE_NAME = "teacher"
                const val ID = "id"
                const val NAME = "name"
                const val CLASSES = "classes"
                val _PROJECTION = arrayOf(ID,NAME,CLASSES)
            }

            object TestDB : BaseColumns {
                const val TABLE_NAME = "tests"
                const val ID = "id"
                const val CLASSID = "class_id"
                const val DATE = "date"
                const val WRONGWORDS = "wrongWords"
                const val WORDS = "words"
                val _PROJECTION = arrayOf(ID,CLASSID,DATE,WRONGWORDS,WORDS)
            }

            object WebDB : BaseColumns {
                const val TABLE_NAME = "webs"
                const val ID = "id"
                const val ADDRESS = "address"
                const val NAME = "name"
                const val CLASSID = "class_id"
                val _PROJECTION = arrayOf(ID,ADDRESS,NAME,CLASSID)
            }

            object WordDB : BaseColumns {
                const val TABLE_NAME = "words"
                const val FROM = "fromVC"
                const val ID = "id"
                const val TO = "toVC"
                const val LIST = "list"
                const val LANGUAGE = "language"
                const val CLASSID = "class_id"
                const val NUMFAIL = "numfail"
                const val NUMSUCCES = "numsucces"
                const val LASTTESTED = "lasttested"
                val _PROJECTION = arrayOf(ID,FROM,TO,LIST,LANGUAGE,CLASSID,NUMFAIL,NUMSUCCES,LASTTESTED)
            }

            object SyncDB: BaseColumns{
                const val TABLE_NAME = "to_sync"
                const val WORD_DB = WordDB.TABLE_NAME
                const val HOMEWORK_DB = HomeWorkDB.TABLE_NAME
                const val TEST_DB = TestDB.TABLE_NAME
                const val WEB_DB = WebDB.TABLE_NAME
                const val STUDENT_DB = StudentDB.TABLE_NAME
                const val TEACHER_DB = TeacherDB.TABLE_NAME
                val _PROJECTION = arrayOf(WORD_DB, HOMEWORK_DB, TEST_DB, WEB_DB, STUDENT_DB, TEACHER_DB)
            }

            object SyncDBrem: BaseColumns{
                const val TABLE_NAME = "to_sync_REM"
                const val WORD_DB = WordDB.TABLE_NAME
                const val HOMEWORK_DB = HomeWorkDB.TABLE_NAME
                const val TEST_DB = TestDB.TABLE_NAME
                const val WEB_DB = WebDB.TABLE_NAME
                const val STUDENT_DB = StudentDB.TABLE_NAME
                const val TEACHER_DB = TeacherDB.TABLE_NAME
                val _PROJECTION = arrayOf(WORD_DB, HOMEWORK_DB, TEST_DB, WEB_DB, STUDENT_DB, TEACHER_DB)
            }

            const val SQL_CREATE_ENTRIES_HOMEWORK =
                "CREATE TABLE ${HomeWorkDB.TABLE_NAME} " +
                        "(${HomeWorkDB.ID} TEXT PRIMARY KEY, " +
                        "${HomeWorkDB.NAME} TEXT, " +
                        "${HomeWorkDB.DATECOMPLETED} TEXT, " +
                        "${HomeWorkDB.DATE} TEXT, " +
                        "${HomeWorkDB.CLASSID} TEXT, " +
                        "${HomeWorkDB.INTERNALID} TEXT, " +
                        "${HomeWorkDB.STUDENT_ID} TEXT, " +
                        "${HomeWorkDB.COMPLETED} TEXT, " +
                        "${HomeWorkDB.WORDLIST} TEXT, " +
                        "${HomeWorkDB.COMPLETEDWORDS} TEXT, " +
                        "${HomeWorkDB.PROBLEMATICWORDS} TEXT)"
            const val SQL_CREATE_ENTRIES_STUDENT =
                "CREATE TABLE ${StudentDB.TABLE_NAME} (" +
                        "${StudentDB.ID} TEXT PRIMARY KEY, " +
                        "${StudentDB.CLASSID} TEXT, " +
                        "${StudentDB.NAME} TEXT)"
            const val SQL_CREATE_ENTRIES_TEACHER =
                "CREATE TABLE ${TeacherDB.TABLE_NAME} (" +
                        "${TeacherDB.ID} TEXT PRIMARY KEY, " +
                        "${TeacherDB.NAME} TEXT, " +
                        "${TeacherDB.CLASSES} TEXT)"
            const val SQL_CREATE_ENTRIES_TEST =
                "CREATE TABLE ${TestDB.TABLE_NAME} (" +
                        "${TestDB.ID} TEXT PRIMARY KEY, " +
                        "${TestDB.CLASSID} TEXT, " +
                        "${TestDB.DATE} TEXT, " +
                        "${TestDB.WRONGWORDS} TEXT, " +
                        "${TestDB.WORDS} TEXT)"
            const val SQL_CREATE_ENTRIES_WEB =
                "CREATE TABLE ${WebDB.TABLE_NAME} (" +
                        "${WebDB.ID} TEXT PRIMARY KEY, " +
                        "${WebDB.ADDRESS} TEXT, " +
                        "${WebDB.NAME} TEXT, " +
                        "${WebDB.CLASSID} TEXT)"
            const val SQL_CREATE_ENTRIES_WORD =
                "CREATE TABLE ${WordDB.TABLE_NAME} (" +
                        "${WordDB.ID} TEXT PRIMARY KEY, " +
                        "${WordDB.FROM} TEXT, " +
                        "${WordDB.TO} TEXT, " +
                        "${WordDB.LIST} TEXT, " +
                        "${WordDB.LANGUAGE} TEXT, " +
                        "${WordDB.CLASSID} TEXT, " +
                        "${WordDB.NUMFAIL} INTEGER, " +
                        "${WordDB.NUMSUCCES} INTEGER, " +
                        "${WordDB.LASTTESTED} TEXT)"
            const val SQL_CREATE_ENTRIES_SYNC =
                "CREATE TABLE ${SyncDB.TABLE_NAME} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                        "${SyncDB.HOMEWORK_DB} TEXT, " +
                        "${SyncDB.STUDENT_DB} TEXT, " +
                        "${SyncDB.TEST_DB} TEXT, " +
                        "${SyncDB.WORD_DB} TEXT, " +
                        "${SyncDB.WEB_DB} TEXT, " +
                        "${SyncDB.TEACHER_DB} TEXT)"

            const val SQL_CREATE_ENTRIES_SYNC_REM =
                "CREATE TABLE ${SyncDBrem.TABLE_NAME} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
                        "${SyncDBrem.HOMEWORK_DB} TEXT, " +
                        "${SyncDBrem.STUDENT_DB} TEXT, " +
                        "${SyncDBrem.TEST_DB} TEXT, " +
                        "${SyncDBrem.WORD_DB} TEXT, " +
                        "${SyncDBrem.WEB_DB} TEXT, " +
                        "${SyncDBrem.TEACHER_DB} TEXT)"

            const val SQL_DELETE_ENTRIES_HOMEWORK = "DROP TABLE IF EXISTS ${HomeWorkDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_STUDENT = "DROP TABLE IF EXISTS ${StudentDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_TEACHER = "DROP TABLE IF EXISTS ${TeacherDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_TEST = "DROP TABLE IF EXISTS ${TestDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_WEB = "DROP TABLE IF EXISTS ${WebDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_WORD = "DROP TABLE IF EXISTS ${WordDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_SYNC = "DROP TABLE IF EXISTS ${SyncDB.TABLE_NAME}"
            const val SQL_DELETE_ENTRIES_SYNC_REM = "DROP TABLE IF EXISTS ${SyncDBrem.TABLE_NAME}"
        }

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_HOMEWORK)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_STUDENT)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_TEACHER)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_TEST)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_WEB)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_WORD)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_SYNC)
            db.execSQL(dbNamesapce.SQL_CREATE_ENTRIES_SYNC_REM)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_HOMEWORK)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_STUDENT)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_TEACHER)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_TEST)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_WEB)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_WORD)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_SYNC)
            db.execSQL(dbNamesapce.SQL_DELETE_ENTRIES_SYNC_REM)
            onCreate(db)
        }

        override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            onUpgrade(db, oldVersion, newVersion)
        }

        companion object {
            const val DATABASE_VERSION = 1
            const val DATABASE_NAME = "3N.db"
        }
    }

    /**
     * @return returns if object with [id] exists in [table]
     */
    fun exist(id: String, table: String, projection: Array<String>, key_name: String): Boolean{
        val db = DB_loc_helper(context).readableDatabase

        val cursor = db.query(
            table,
            projection,
            "$key_name = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                return true
            }
        }

        return false
    }

    /**
     * saves [words] to database
     */
    fun saveWords(words: List<Word>){
        val db = DB_loc_helper(context).writableDatabase

        for(word in words) {
            val values = ContentValues().apply {
                put(DB_loc_helper.dbNamesapce.WordDB.ID, word.id)
                put(DB_loc_helper.dbNamesapce.WordDB.FROM, word.from)
                put(DB_loc_helper.dbNamesapce.WordDB.TO, word.to)
                put(DB_loc_helper.dbNamesapce.WordDB.LANGUAGE, word.language)
                put(DB_loc_helper.dbNamesapce.WordDB.LIST, word.list)
                put(DB_loc_helper.dbNamesapce.WordDB.NUMSUCCES, word.stats!!.numSucces)
                put(DB_loc_helper.dbNamesapce.WordDB.NUMFAIL, word.stats!!.numFail)
                put(DB_loc_helper.dbNamesapce.WordDB.LASTTESTED, DateUtils.fromDate2String(word.stats!!.lastTested))
                put(DB_loc_helper.dbNamesapce.WordDB.CLASSID, word.class_id)
            }

            if(!exist(word.id,DB_loc_helper.dbNamesapce.WordDB.TABLE_NAME,
                    DB_loc_helper.dbNamesapce.WordDB._PROJECTION, DB_loc_helper.dbNamesapce.WordDB.ID)){
                db?.insert(DB_loc_helper.dbNamesapce.WordDB.TABLE_NAME, null, values)
            }
        }

        db.close()
    }

    /**
     * saves homework to individual students
     */
    fun saveHomework(homework: Map<HomeWork, Student>){
        val db = DB_loc_helper(context).writableDatabase

        for((h,s) in homework){
            val values = ContentValues().apply {
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.ID, h.id)
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.NAME, h.name)
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.CLASSID, h.class_id)
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.COMPLETED, h.completed)
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.INTERNALID, h.homework_id)
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.STUDENT_ID, s.id)
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.DATE, DateUtils.fromDate2String(h.date))
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.DATECOMPLETED, DateUtils.fromDate2String(h.dateCompleted))
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.WORDLIST, VocabularyUtils.fromArrayToString(h.wordList))
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.COMPLETEDWORDS, VocabularyUtils.fromArrayToString(h.completedWords))
                put(DB_loc_helper.dbNamesapce.HomeWorkDB.PROBLEMATICWORDS, VocabularyUtils.fromArrayToString(h.completedWords))
            }

            if(exist(h.id,DB_loc_helper.dbNamesapce.HomeWorkDB.TABLE_NAME,DB_loc_helper.dbNamesapce.HomeWorkDB._PROJECTION,DB_loc_helper.dbNamesapce.HomeWorkDB.ID)){
                val selection = "${DB_loc_helper.dbNamesapce.HomeWorkDB.ID} LIKE ?"
                val selectionArgs = arrayOf(h.id)
                db?.update(DB_loc_helper.dbNamesapce.HomeWorkDB.TABLE_NAME, values,selection,selectionArgs)
            }
            else {
                db?.insert(DB_loc_helper.dbNamesapce.HomeWorkDB.TABLE_NAME, null, values)
            }
        }

        db.close()
    }

    /**
     * saves list of webs
     */
    fun saveWeb(webs: List<Web>){
        val db = DB_loc_helper(context).writableDatabase

        for(web in webs){
            val values = ContentValues().apply {
                put(DB_loc_helper.dbNamesapce.WebDB.ID, web.id)
                put(DB_loc_helper.dbNamesapce.WebDB.NAME, web.name)
                put(DB_loc_helper.dbNamesapce.WebDB.ADDRESS, web.address)
                put(DB_loc_helper.dbNamesapce.WebDB.CLASSID, web.class_id)
            }

            if(exist(web.id,DB_loc_helper.dbNamesapce.WebDB.TABLE_NAME,
                    DB_loc_helper.dbNamesapce.WebDB._PROJECTION,
                    DB_loc_helper.dbNamesapce.WebDB.ID)){
                val selection = "${DB_loc_helper.dbNamesapce.WebDB.ID} LIKE ?"
                val selectionArgs = arrayOf(web.id)
                db?.update(DB_loc_helper.dbNamesapce.WebDB.TABLE_NAME, values, selection, selectionArgs)
            }
            else {
                db?.insert(DB_loc_helper.dbNamesapce.WebDB.TABLE_NAME, null, values)
            }
        }

        db.close()
    }

    /**
     * saves completed test's list
     */
    fun saveTest(tests: List<Test>){
        val db = DB_loc_helper(context).writableDatabase

        for(test in tests){
            val values = ContentValues().apply {
                put(DB_loc_helper.dbNamesapce.TestDB.ID, test.id)
                put(DB_loc_helper.dbNamesapce.TestDB.CLASSID, test.class_id)
                put(DB_loc_helper.dbNamesapce.TestDB.DATE, DateUtils.fromDate2String(test.date))
                put(DB_loc_helper.dbNamesapce.TestDB.WORDS, VocabularyUtils.fromArrayToString(test.words))
                put(DB_loc_helper.dbNamesapce.TestDB.WRONGWORDS, VocabularyUtils.fromArrayToString(test.wrongWords))
            }

            if(exist(test.id, DB_loc_helper.dbNamesapce.TestDB.TABLE_NAME, DB_loc_helper.dbNamesapce.TestDB._PROJECTION, DB_loc_helper.dbNamesapce.TestDB.ID)){
                val selection = "${DB_loc_helper.dbNamesapce.TestDB.ID} LIKE ?"
                val selectionArgs = arrayOf(test.id)
                db?.update(DB_loc_helper.dbNamesapce.TestDB.TABLE_NAME, values, selection, selectionArgs)
            }
            else{
                db?.insert(DB_loc_helper.dbNamesapce.TestDB.TABLE_NAME, null, values)
            }
        }

        db.close()
    }

    /**
     * saves students but only properties of student class, homework, webs and other objects need
     * to be saved separately
     */
    fun saveStudent(students: List<Student>){
        val db = DB_loc_helper(context).writableDatabase

        for(student in students){
            val values = ContentValues().apply {
                put(DB_loc_helper.dbNamesapce.StudentDB.ID, student.id)
                put(DB_loc_helper.dbNamesapce.StudentDB.CLASSID, student.class_id)
                put(DB_loc_helper.dbNamesapce.StudentDB.NAME, student.name)
            }

            if(exist(student.id, DB_loc_helper.dbNamesapce.StudentDB.TABLE_NAME, DB_loc_helper.dbNamesapce.StudentDB._PROJECTION, DB_loc_helper.dbNamesapce.StudentDB.ID)){
                val selection = "${DB_loc_helper.dbNamesapce.StudentDB.ID} LIKE ?"
                val selectionArgs = arrayOf(student.id)
                db?.update(DB_loc_helper.dbNamesapce.StudentDB.TABLE_NAME, values, selection, selectionArgs)
            }
            else{
                db?.insert(DB_loc_helper.dbNamesapce.StudentDB.TABLE_NAME, null, values)
            }
        }

        db.close()
    }

    /**
     * save properties of teacher class
     */
    fun saveTeacher(teacher: Teacher){
        val db = DB_loc_helper(context).writableDatabase
        val idArray = mutableListOf<String>()
        for(c in teacher.classes){
            idArray += c.id
        }
        val classes = idArray.joinToString(";")

        val values = ContentValues().apply {
            put(DB_loc_helper.dbNamesapce.TeacherDB.ID, teacher.id)
            put(DB_loc_helper.dbNamesapce.TeacherDB.NAME, teacher.name)
            put(DB_loc_helper.dbNamesapce.TeacherDB.CLASSES, classes)
        }

        if(exist(teacher.id, DB_loc_helper.dbNamesapce.TeacherDB.TABLE_NAME, DB_loc_helper.dbNamesapce.TeacherDB._PROJECTION, DB_loc_helper.dbNamesapce.TeacherDB.ID)){
            val selection = "${DB_loc_helper.dbNamesapce.TeacherDB.ID} LIKE ?"
            val selectionArgs = arrayOf(teacher.id)
            db?.update(DB_loc_helper.dbNamesapce.TeacherDB.TABLE_NAME, values, selection, selectionArgs)
        }
        else{
            db?.insert(DB_loc_helper.dbNamesapce.TeacherDB.TABLE_NAME, null, values)
        }

        db.close()
    }

    /**
     * saves objects to add/update to remote database
     */
    fun saveSync(sync: List<Syncable>){
        val db = DB_loc_helper(context).writableDatabase

        for(s in sync){
            val values = ContentValues().apply {
                when(s.getType()){
                    Syncable.Types.WebPage -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDB.WEB_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Homework -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDB.HOMEWORK_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Test -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDB.TEST_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Vocabulary -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDB.WORD_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Student -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDB.STUDENT_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Teacher -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDB.TEACHER_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, null, values)
                    }
                }
            }
        }

        db.close()
    }

    /**
     * saves objects to remove from remote database
     */
    fun saveSyncRem(sync: List<Syncable>){
        val db = DB_loc_helper(context).writableDatabase

        for(s in sync){
            val values = ContentValues().apply {
                when(s.getType()){
                    Syncable.Types.WebPage -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDBrem.WEB_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Homework -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDBrem.HOMEWORK_DB, (s as HomeWork).homework_id)
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Test -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDBrem.TEST_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Vocabulary -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDBrem.WORD_DB, VocabularyUtils.addList(s as Word))
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Student -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDBrem.STUDENT_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, null, values)
                    }
                    Syncable.Types.Teacher -> {
                        val values = ContentValues().apply {
                            put(DB_loc_helper.dbNamesapce.SyncDBrem.TEACHER_DB, s.getID())
                        }

                        db?.insert(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, null, values)
                    }
                }
            }
        }

        db.close()
    }

    /**
     * @return load words for [class_id]
     */
    fun getWords(class_id: String): MutableList<Word>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<Word>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.WordDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.WordDB._PROJECTION,
            "${DB_loc_helper.dbNamesapce.WordDB.CLASSID} = ?",
            arrayOf(class_id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                val from = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.FROM))
                val to = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.TO))
                val list = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.LIST))
                val language = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.LANGUAGE))
                val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.ID))
                val lastTested = DateUtils.fromString(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.LASTTESTED)))
                val numFail = getInt(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.NUMFAIL))
                val numSuccess = getInt(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WordDB.NUMSUCCES))

                val word = Word(id,from,to,list,language,class_id,_stats = Word.WordStats(numFail,numSuccess,lastTested))
                toReturn.add(word)
            }
        }

        db.close()
        return toReturn
    }

    /**
     * loads homework based on [class_id] and optionally on student_id
     */
    fun getHomework(class_id: String, student_id: String? = null): MutableList<HomeWork>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<HomeWork>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.HomeWorkDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.HomeWorkDB._PROJECTION,
            if(student_id == null) "${DB_loc_helper.dbNamesapce.HomeWorkDB.CLASSID} = ?"
                else "${DB_loc_helper.dbNamesapce.HomeWorkDB.CLASSID} = ? AND ${DB_loc_helper.dbNamesapce.HomeWorkDB.STUDENT_ID} = ?",
            if(student_id == null) arrayOf(class_id)
                else arrayOf(class_id, student_id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.ID))
                val name = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.NAME))
                val dateCompleted = DateUtils.fromString(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.DATECOMPLETED)))
                val internal_id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.INTERNALID))
                val date = DateUtils.fromString(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.DATE)))
                val completed = getInt(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.COMPLETED)) == 1
                val wordList = VocabularyUtils.fromStringToArray(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.WORDLIST)))
                val completedWords = mutableListOf<Word>()
                val problematicWords = mutableListOf<Word>()

                for(word in VocabularyUtils.fromStringToArray(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.COMPLETEDWORDS)))){
                    if(wordList.containsFromList(word)) completedWords.add(wordList.indexFromList(word)!!)
                }

                for(word in VocabularyUtils.fromStringToArray(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.HomeWorkDB.PROBLEMATICWORDS)))){
                    if(wordList.containsFromList(word)) problematicWords.add(wordList.indexFromList(word)!!)
                }

                toReturn.add(HomeWork(id,name,date,wordList.toMutableList(),dateCompleted,class_id,internal_id,completed,completedWords, problematicWords))
            }
        }

        db.close()
        return toReturn
    }

    /**
     * @return list of webs
     */
    fun getWeb(class_id: String): MutableList<Web>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<Web>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.WebDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.WebDB._PROJECTION,
            "${DB_loc_helper.dbNamesapce.WebDB.CLASSID} = ?",
            arrayOf(class_id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WebDB.ID))
                val address = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WebDB.ADDRESS))
                val name = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.WebDB.NAME))

                toReturn.add(Web(id,address,name,class_id))
            }
        }

        db.close()
        return toReturn
    }

    /**
     * @return returns list of test
     */
    fun getTest(class_id: String): MutableList<Test>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<Test>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.TestDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.TestDB._PROJECTION,
            "${DB_loc_helper.dbNamesapce.TestDB.CLASSID} = ?",
            arrayOf(class_id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.TestDB.ID))
                val date = DateUtils.fromString(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.TestDB.DATE)))
                val wrongWords = VocabularyUtils.fromStringToArray(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.TestDB.WRONGWORDS)))
                val words = VocabularyUtils.fromStringToArray(getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.TestDB.WORDS)))

                toReturn.add(Test(id,words.toMutableList(),class_id,date,wrongWords.toMutableList()))
            }
        }

        db.close()
        return toReturn
    }

    /**
     * returns student object based on id which can be unique for object or it can be class_id
     * which one is it determines [classIDsearch]
     */
    fun getStudent(id: String, classIDsearch: Boolean = false): MutableList<Student>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<Student>()

        val searchSymbol = if(classIDsearch) DB_loc_helper.dbNamesapce.StudentDB.CLASSID else DB_loc_helper.dbNamesapce.StudentDB.ID

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.StudentDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.StudentDB._PROJECTION,
            "$searchSymbol = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.StudentDB.ID))
                val class_id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.StudentDB.CLASSID))
                val name = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.StudentDB.NAME))

                toReturn.add(Student(id,name,mutableListOf<Word>(),mutableListOf<Web>(),mutableListOf<HomeWork>(),class_id))
            }
        }

        db.close()
        return toReturn
    }

    /**
     * @return returns teacher based on [id]
     */
    fun getTeacher(id: String): Teacher?{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<Teacher>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.TeacherDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.TeacherDB._PROJECTION,
            "${DB_loc_helper.dbNamesapce.TeacherDB.ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                val name = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.TeacherDB.NAME))
                val classes = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.TeacherDB.CLASSES)).split(";")
                var classesFinal = mutableListOf<Class>()
                for(c in classes){
                    classesFinal.add(Class(c,listOf<Student>()))
                }

                toReturn.add(Teacher(id,name, classesFinal))
            }
        }

        db.close()
        return if(toReturn.count() == 0) null else toReturn[0]
    }

    /**
     * @return objects to add/update for given [type]
     */
    fun getSync(type: Syncable.Types): List<String>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<String>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME,
            DB_loc_helper.dbNamesapce.SyncDB._PROJECTION,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                when(type){
                    Syncable.Types.Teacher ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDB.TEACHER_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Student ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDB.STUDENT_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Vocabulary ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDB.WORD_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Test ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDB.TEST_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Homework ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDB.HOMEWORK_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.WebPage ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDB.WEB_DB))
                        id?.let { toReturn += id }
                    }
                }
            }
        }

        db.close()
        return toReturn
    }

    /**
     * @return objects to remove for given [type]
     */
    fun getSyncREM(type: Syncable.Types): List<String>{
        val db = DB_loc_helper(context).readableDatabase
        val toReturn = mutableListOf<String>()

        val cursor = db.query(
            DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME,
            DB_loc_helper.dbNamesapce.SyncDBrem._PROJECTION,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor){
            while(moveToNext()){
                when(type){
                    Syncable.Types.Teacher ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDBrem.TEACHER_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Student ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDBrem.STUDENT_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Vocabulary ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDBrem.WORD_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Test ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDBrem.TEST_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.Homework ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDBrem.HOMEWORK_DB))
                        id?.let { toReturn += id }
                    }
                    Syncable.Types.WebPage ->{
                        val id = getString(getColumnIndexOrThrow(DB_loc_helper.dbNamesapce.SyncDBrem.WEB_DB))
                        id?.let { toReturn += id }
                    }
                }
            }
        }

        db.close()
        return toReturn
    }

    /**
     * deletes table for given [tables]
     */
    fun deleteTable(vararg tables: Syncable.Types){
        val db = DB_loc_helper(context).writableDatabase
        for(table in tables){
            when(table){
                Syncable.Types.Vocabulary -> db?.delete(DB_loc_helper.dbNamesapce.WordDB.TABLE_NAME, null, null)
                Syncable.Types.WebPage -> db?.delete(DB_loc_helper.dbNamesapce.WebDB.TABLE_NAME, null, null)
                Syncable.Types.Homework -> db?.delete(DB_loc_helper.dbNamesapce.HomeWorkDB.TABLE_NAME, null, null)
                Syncable.Types.Test -> db?.delete(DB_loc_helper.dbNamesapce.TestDB.TABLE_NAME, null, null)
                Syncable.Types.Student -> db?.delete(DB_loc_helper.dbNamesapce.StudentDB.TABLE_NAME, null, null)
                Syncable.Types.Teacher -> db?.delete(DB_loc_helper.dbNamesapce.TeacherDB.TABLE_NAME, null, null)
            }
        }

        db.close()
    }

    /**
     * deletes objects in [syncList] from database
     */
    fun deleteObj(syncList: List<Syncable>){
        val db = DB_loc_helper(context).writableDatabase

        for(sync in syncList) {
            val selectionArgs = arrayOf(if(sync.getType() != Syncable.Types.Homework) sync.getID() else (sync as HomeWork).homework_id)
            when (sync.getType()) {
                Syncable.Types.WebPage -> {
                    val selection = "${DB_loc_helper.dbNamesapce.WebDB.ID} LIKE ?"
                    db?.delete(DB_loc_helper.dbNamesapce.WebDB.TABLE_NAME, selection, selectionArgs)
                }
                Syncable.Types.Homework -> {
                    val selection = "${DB_loc_helper.dbNamesapce.HomeWorkDB.INTERNALID} LIKE ?"
                    db?.delete(
                        DB_loc_helper.dbNamesapce.HomeWorkDB.TABLE_NAME,
                        selection,
                        selectionArgs
                    )
                }
                Syncable.Types.Test -> {
                    val selection = "${DB_loc_helper.dbNamesapce.TestDB.ID} LIKE ?"
                    db?.delete(
                        DB_loc_helper.dbNamesapce.TestDB.TABLE_NAME,
                        selection,
                        selectionArgs
                    )
                }
                Syncable.Types.Vocabulary -> {
                    val selection = "${DB_loc_helper.dbNamesapce.WordDB.ID} LIKE ?"
                    db?.delete(
                        DB_loc_helper.dbNamesapce.WordDB.TABLE_NAME,
                        selection,
                        selectionArgs
                    )
                }
                Syncable.Types.Student -> {
                    val selection = "${DB_loc_helper.dbNamesapce.StudentDB.ID} LIKE ?"
                    db?.delete(
                        DB_loc_helper.dbNamesapce.StudentDB.TABLE_NAME,
                        selection,
                        selectionArgs
                    )
                }
                Syncable.Types.Teacher -> {
                    val selection = "${DB_loc_helper.dbNamesapce.TeacherDB.ID} LIKE ?"
                    db?.delete(
                        DB_loc_helper.dbNamesapce.TeacherDB.TABLE_NAME,
                        selection,
                        selectionArgs
                    )
                }
            }
        }

        db.close()
    }

    /**
     * deletes all object of type [toDelete] to add/update to remote database
     */
    fun deleteSync(vararg toDelete: Syncable.Types){
        val db = DB_loc_helper(context).writableDatabase
        var selection = ""

        for(t in toDelete){
            when(t){
                Syncable.Types.Teacher -> selection = "${DB_loc_helper.dbNamesapce.SyncDB.TEACHER_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDB.TEACHER_DB} != ''"
                Syncable.Types.Student -> selection = "${DB_loc_helper.dbNamesapce.SyncDB.STUDENT_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDB.STUDENT_DB} != ''"
                Syncable.Types.Vocabulary -> selection = "${DB_loc_helper.dbNamesapce.SyncDB.WORD_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDB.WORD_DB} != ''"
                Syncable.Types.Test -> selection = "${DB_loc_helper.dbNamesapce.SyncDB.TEST_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDB.TEST_DB} != ''"
                Syncable.Types.Homework -> selection = "${DB_loc_helper.dbNamesapce.SyncDB.HOMEWORK_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDB.WEB_DB} != ''"
                Syncable.Types.WebPage -> selection = "${DB_loc_helper.dbNamesapce.SyncDB.WEB_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDB.WEB_DB} != ''"
            }

            db?.delete(DB_loc_helper.dbNamesapce.SyncDB.TABLE_NAME, selection, null)
        }

        db.close()
    }

    /**
     * deletes all object of type [toDelete] to remove from remote database
     */
    fun deleteSyncREM(vararg toDelete: Syncable.Types){
        val db = DB_loc_helper(context).writableDatabase
        var selection = ""

        for(t in toDelete){
            when(t){
                Syncable.Types.Teacher -> selection = "${DB_loc_helper.dbNamesapce.SyncDBrem.TEACHER_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDBrem.TEACHER_DB} != ''"
                Syncable.Types.Student -> selection = "${DB_loc_helper.dbNamesapce.SyncDBrem.STUDENT_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDBrem.STUDENT_DB} != ''"
                Syncable.Types.Vocabulary -> selection = "${DB_loc_helper.dbNamesapce.SyncDBrem.WORD_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDBrem.WORD_DB} != ''"
                Syncable.Types.Test -> selection = "${DB_loc_helper.dbNamesapce.SyncDBrem.TEST_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDBrem.TEST_DB} != ''"
                Syncable.Types.Homework -> selection = "${DB_loc_helper.dbNamesapce.SyncDBrem.HOMEWORK_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDBrem.WEB_DB} != ''"
                Syncable.Types.WebPage -> selection = "${DB_loc_helper.dbNamesapce.SyncDBrem.WEB_DB} IS NOT NULL OR ${DB_loc_helper.dbNamesapce.SyncDBrem.WEB_DB} != ''"
            }

            db?.delete(DB_loc_helper.dbNamesapce.SyncDBrem.TABLE_NAME, selection, null)
        }

        db.close()
    }
}