package com.example.vocab.basic

import java.util.*

/**
 * Class that represents test's data
 *
 * @property id identification string
 * @property words words to test
 * @property class_id same as class_id for student
 * @property date date on which the test was taken
 * @property wrongWords words that user didn't know
 * @property homeworkID if test was part of homework otherwise this parameter is by default null
 */
open class Test(val id: String, val words: List<Word>, val class_id: String, val date: Date = Date(),
                wrongWords:List<Word> = listOf(), val homeworkID: String? = null)
    :Syncable{
    open var wrongWords = wrongWords
        protected set

    val wordsCount: Int
        get() = words.count()

    /**
     * Copy constructor
     */
    fun copy(id: String = this.id, words: List<Word> = this.words, class_id: String = this.class_id, date: Date = this.date,
                wrongWords: List<Word> = this.wrongWords, homeworkID: String? = this.homeworkID): Test{
        return Test(id, words, class_id, date, wrongWords, homeworkID)
    }

    override fun getID(): String{
        return id
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.Test
    }
}