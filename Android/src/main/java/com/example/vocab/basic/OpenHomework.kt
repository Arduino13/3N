package com.example.vocab.basic

import java.util.*
/**
 * Version of class that holds all necessary data for homework, but open version, which allows
 * to modify some variables of the class
 *
 * @property id identification string of homework object for one student
 * @property name name of the homework
 * @property date date when homework was assigned
 * @property wordList list of words to learn
 * @property dateCompleted due date of homework TODO: rename
 * @property class_id id of class
 * @property completed variable to check if homework is completed
 * @property completedWords list of completed words
 * @property problematicWords list of words that caused problems during tests
 *
 * @constructor second one is for converting closed homework class to open one
 */
class OpenHomework(id: String, name: String, date: Date, wordList: List<Word>,
                   dateCompleted: Date, class_id: String, internal_id: String,
                   _completed: Boolean = false, _completedWords: List<Word> = listOf<Word>(), problematicWords: List<Word>? = null):
    HomeWork(id, name, date, wordList, dateCompleted, class_id, internal_id,
        _completed, _completedWords, problematicWords){

    constructor(homework: HomeWork) : this(homework.id, homework.name, homework.date, homework.wordList,
        homework.dateCompleted, homework.class_id, homework.homework_id,homework.completed, homework.completedWords, homework.problematicWords)

    override var completed: Boolean = _completed
        public set

    override var completedWords: List<Word> = _completedWords
        public set
}