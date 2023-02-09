package com.example.vocab.basic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.Date

/**
 * Version of class that holds all necessary data for word, but open version, which allows
 * to modify some variables of the class
 *
 * @property id identification string
 * @property from word in foreign language
 * @property to translation of the word to application's language
 * @property list list to which the world is assigned
 * @property language language from the world is translated, for now unused
 * @property class_id same as class_id of student
 * @property newList used when creating new list that doesn't contain any words
 * @property _stats statistics for word
 */
class OpenWord(id: String, from: String, to: String = "", list: String = "", language: String = "", class_id: String = "", newList: Boolean = false,
               _stats: Word.WordStats = Word.WordStats()) : Word(id, from, to, list, language, class_id, newList) {
    var openStats: OpenWordStats = OpenWordStats(_stats)
        set(new){
            field = new
            stats = new
        }

    init{
        stats = openStats
    }

    constructor(word: Word): this(word.id, word.from, word.to, word.list, word.language, word.class_id, word.newList, word.stats)

    class OpenWordStats(_numFail: Int = 0, _numSucces: Int = 0,
                        _lastTested: Date = Date()): WordStats(){
        constructor(stats: WordStats): this(stats.numFail, stats.numSucces, stats.lastTested)

        override var numFail: Int = _numFail
            public set
        override var numSucces: Int = _numSucces
            public set
        override var lastTested: Date = _lastTested
            public set
    }
}