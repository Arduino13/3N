package com.example.vocab.basic

import java.util.*

/**
 * Class that represent word
 *
 * @property id identification string
 * @property from word in foreign language
 * @property to translation of word to application's language
 * @property list list to which the world is assigned
 * @property language language from the world is translated, for now unused
 * @property class_id same as class_id of student
 * @property newList used when creating new list that doesn't contain any words
 * @property _stats statistics for word
 */
open class Word(val id: String, val from: String, val to: String = "", val list: String = "", val language: String = "", val class_id: String = "", val newList: Boolean = false,
                val _stats: WordStats = WordStats()): Syncable{
    /**
     * Class for word statistics
     *
     * @property _numFail number of times the user didn't know the word
     * @property _numSucces number of times the user knew the word
     * @property _lastTested last time word was put to the test, it's used to so that the same world doesn't
     * appear to often
     */
    open class WordStats(_numFail: Int = 0, _numSucces: Int = 0,
                          _lastTested: Date = Date(1)
    ){
        open var numFail = _numFail
            protected set
        open var numSucces = _numSucces
            protected set
        open var lastTested = _lastTested
            protected set
    }

    open var stats = _stats
        protected set

    /**
     * copy constructor
     */
    fun copy(from: String = this.from, to: String = this.to, list: String = this.list, language: String = this.language, class_id: String = this.class_id, id: String = this.id,
        newList: Boolean = this.newList, _stats: WordStats = this.stats): Word{
        return Word(id, from, to, list, language, class_id, newList, _stats)
    }

    override fun getID(): String{
        return id
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.Vocabulary
    }
}