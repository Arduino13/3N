package com.example.vocab.basic

/**
 * Class which holds students within the same class
 *
 * @property id identification string of the class
 * @property name name of the class
 * @property students list of students
 */
data class Class(val id: String, val students: List<Student>): Syncable{
    val name: String
    init{
        val begin = id.indexOf("_")
        name = if(begin != -1) {
            id.substring(0, begin)
        } else{
            ""
        }
    }

    override fun getID(): String{
        return id
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.Class
    }
}