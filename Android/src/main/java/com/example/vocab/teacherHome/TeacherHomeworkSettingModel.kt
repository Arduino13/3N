package com.example.vocab.teacherHome

import androidx.lifecycle.ViewModel
import java.util.*

/**
 * Model for storing data between fragment transitions
 *
 * @property nameOfHomework name of the homework
 * @property dateOfHomework due date for homework
 * @property dateHeaderOfHomework text representation of [dateOfHomework]
 */

class TeacherHomeworkSettingModel: ViewModel(){
    var nameOfHomework: String? = null
    var dateOfHomework: Date? = null
    var dateHeaderOfHomework: String? = null
}