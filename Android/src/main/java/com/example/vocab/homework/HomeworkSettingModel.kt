package com.example.vocab.homework

import androidx.lifecycle.ViewModel
import com.example.vocab.basic.Word
import java.util.*

/**
 * Model for data exchange between fragments
 *
 * @property listOfWords for storing list of words for [HomeworkTableWords], when changing orientation
 * of a screen
 * @property calendarCurrentDate holds calendar current date in case of displaying [HorizontalHomeworkFragment]
 */
class HomeworkSettingModel: ViewModel() {
    var listOfWords: List<Word>? = null
    var calendarCurrentDate: Date? = null
}