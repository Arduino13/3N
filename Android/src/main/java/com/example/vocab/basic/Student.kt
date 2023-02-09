package com.example.vocab.basic

/**
 * Class that holds students data like homework, words, webs, tests, and provides methods to
 * manipulate with tem
 *
 * @property id identification string of student
 * @property name name of the student
 * @property listWords student's list of words
 * @property listWebs student's list of webs
 * @property listHomework student's homework
 * @property class_id identification of string in to which student is assigned
 * @property new decides if student object is newly created and all objects inside it need to be synchronized
 * or if its created for example by a database on loading
 * @property listTests list of tests that student had taken, but only those that had been taken since of opening of application
 */
open class Student: StudentSync, Syncable{
    var id: String
        protected set
    var class_id: String
        protected set
    var name: String
        protected set
    var listWords: MutableList<Word>
        protected set
    var listWebs: MutableList<Web>
        protected set
    var listHomework: MutableList<HomeWork>
        protected set
    var listTests = mutableListOf<Test>()
        protected set

    protected constructor(): super(){
        id = ""
        class_id = ""
        name = ""
        listWords = mutableListOf()
        listWebs = mutableListOf()
        listHomework = mutableListOf()
    }

    constructor(id: String, name: String, listWords : MutableList<Word>, listWebs: MutableList<Web>,
                listHomework: MutableList<HomeWork>, class_id: String, new: Boolean = false,
                tests: MutableList<Test>? = null): super() {

        this.id = id
        this.name = name
        this.listWords = listWords
        this.listWebs = listWebs
        this.listHomework = listHomework
        this.class_id = class_id

        tests?.let {
            listTests.addAll(tests)
        }

        if(new){
            addToSync(this)
            for(h in this.listHomework){
                addToSync(h)
            }
            for(w in this.listWebs){
                addToSync(w)
            }
            for(word in this.listWords){
                addToSync(word)
            }
            for(test in this.listTests){
                addToSync(test)
            }
        }
    }

    /**
     * Copy constructor
     * @return Student class
     */
    fun copy(id: String = this.id, name: String = this.name, listWords: MutableList<Word> = this.listWords,
             listWebs: MutableList<Web> = this.listWebs, listHomework: MutableList<HomeWork> = this.listHomework,
            class_id: String = this.class_id, listTests:MutableList<Test> =this.listTests): Student{
        return Student(id, name, listWords, listWebs, listHomework, class_id, false, listTests)
    }

    /**
     * function that helps when searching for an object with parent class Syncable
     *
     * [id] identification string of searched object
     * [list] list of objects to search
     * @return Syncable object in case of success or null
     */
    private fun genericSearch(id: String, list: List<Syncable>): Syncable?{
        for(t in list){
            if(t.getID() == id){
                return t
            }
        }

        return null
    }

    /**
     * Add test to listTests and to synchronize
     *
     * [test] Test object
     */
    fun addTest(test: Test){
        listTests.add(test)

        addToSync(test)
    }

    /**
     * Add word to listWords and to synchronize
     *
     * [word] Word object
     */
    fun addWord(word: Word){
        listWords.add(word)
        addToSync(word)
    }

    /**
     * Add list of words to listWords and to synchronize
     *
     * [words] list of Word objects
     */
    fun addWord(words: List<Word>){
        for(w in words){
            addWord(w)
        }
    }

    /**
     * Updates word inside student
     *
     * [word] Word object
     */
    fun updateWord(word: Word){
        listWords.remove(genericSearch(word.id, listWords))
        listWords.add(word)

        addToSync(word, local = true)
    }

    /**
     * Updates list of words inside student
     *
     * [word] list of Word objects
     */
    fun updateWord(word: List<Word>){
        for(w in word){
            updateWord(w)
        }
    }

    /**
     * Updates homework inside student
     *
     * [homework] HomeWork object
     */
    fun updateHomework(homework: HomeWork){
        listHomework.remove(genericSearch(homework.id, listHomework))
        listHomework.add(homework)

        addToSync(homework)
    }

    /**
     * Adds web to student
     *
     * [web] Web object
     */
    fun addWeb(web: Web){
        listWebs.add(web)

        addToSync(web)
    }

    /**
     * Removes web from student
     *
     * [web] Web object
     */
    fun removeWeb(web: Web){
        listWebs.remove(genericSearch(web.id, listWebs))
        removeToSync(web)
    }

    /**
     * Removes word from student
     *
     * [word] Word object
     */
    fun removeWord(word: Word){
        listWords.remove(genericSearch(word.id, listWords))
        removeToSync(word)
    }

    /**
     * Removes word's list from student
     *
     * [words] list of Word objects
     */
    fun removeWord(words: List<Word>){
        for(w in words){
            removeWord(w)
        }
    }

    /**
     * Returns homework object for requested homework_id
     *
     * [homeworkID] identifies homework for class
     * @return homework object if such object with homeworkID exist or null
     */
    fun getHomeworkByHomeworkID(homeworkID: String): HomeWork?{
        for(t in listHomework){
            if(t.homework_id == homeworkID){
                return t
            }
        }

        return null
    }

    /**
     * Returns homework based on id
     *
     * @return homework object if such object with id exist or null
     */
    fun getHomework(id: String): HomeWork?{
        return genericSearch(id, listHomework) as HomeWork?
    }

    /**
     * Returns test based on id
     *
     * @return test object if such object with id exist or null
     */
    fun getTest(id: String): Test?{
        return genericSearch(id, listTests) as Test?
    }

    /**
     * Returns web based on id
     *
     * @return web object if such object with id exist or null
     */
    fun getWeb(id: String): Web?{
        return genericSearch(id, listWebs) as Web?
    }

    /**
     * Returns word based on id
     *
     * @return word object if such object with id exist or null
     */
    fun getWord(id: String): Word?{
        return genericSearch(id, listWords) as Word?
    }

    override fun getID(): String{
        return id
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.Student
    }
}