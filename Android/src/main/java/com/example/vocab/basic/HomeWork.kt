package com.example.vocab.basic

import java.util.*

/**
 * Class that holds all necessary data for homework
 *
 * @property id identification string of homework object for one student
 * @property name name of the homework
 * @property date date when homework was assigned
 * @property wordList list of words to learn
 * @property dateCompleted due date of homework TODO: rename
 * @property class_id id of class
 * @property homework_id identification string of homework for the whole class
 * @property completed variable to check if homework is completed
 * @property completedWords list of learned words
 * @property problematicWords list of words that caused problems during tests
 */
open class HomeWork(val id: String, val name: String, val date: Date, val wordList: List<Word>,
                     val dateCompleted: Date, val class_id: String, val homework_id: String,
                     _completed: Boolean = false, _completedWords: List<Word> = listOf<Word>(),
                    val problematicWords: List<Word>? = null): Syncable {

    open var completed: Boolean = _completed
        protected set
    open var completedWords: List<Word> = _completedWords
        protected set

    /**
     * copy constructor
     * @return returns Homework class
     */
    fun copy(id: String = this.id, name: String = this.name, date: Date = this.date,
             wordList: List<Word> = this.wordList, dateCompleted: Date = this.dateCompleted,
             class_id: String = this.class_id, internal_id: String = this.homework_id,
             completed: Boolean = this.completed, completedWords: List<Word> = this.completedWords): HomeWork{

        return HomeWork(id, name, date, wordList, dateCompleted, class_id, internal_id, completed, completedWords)
    }

    override fun getID(): String{
        return id;
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.Homework
    }
}