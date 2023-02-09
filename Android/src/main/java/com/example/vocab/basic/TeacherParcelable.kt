package com.example.vocab.basic

import android.os.Parcel
import android.os.Parcelable
import java.lang.Exception

/**
 * Class that extends Teacher and is used to transfer teacher data between activities in application
 */
class TeacherParcelable: Teacher, Parcelable {
    val exception = Exception("Internal error parcelable - Teacher")

    constructor(teacher: Teacher): super(
        teacher.id, teacher.name, teacher.classes
    )

    constructor(parcel: Parcel) : super() {
        id = parcel.readString() ?: throw exception
        name = parcel.readString() ?: throw exception
        val listClasses = mutableListOf<Class>()
        for(i in 0 until parcel.readInt()){
            val id = parcel.readString() ?: throw exception
            val listStudents = mutableListOf<Student>()
            for(x in 0 until parcel.readInt()){
                listStudents.add(StudentParcelable.createFromParcel(parcel))
            }

            listClasses.add(Class(
                id,
                listStudents
            ))
        }

        classes = listClasses
    }

    /**
     * Function similar to the one in StudentParcelable class, to write the class of students every
     * student is converted to StudentParcelable and written to parcel
     */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(classes.size)
        for(cls in classes){
            parcel.writeString(cls.id)
            parcel.writeInt(cls.students.size)
            for(student in cls.students){
                val parcStudent = StudentParcelable(student)
                parcStudent.writeToParcel(parcel, flags)
            }
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TeacherParcelable> {
        override fun createFromParcel(parcel: Parcel): TeacherParcelable {
            return TeacherParcelable(parcel)
        }

        override fun newArray(size: Int): Array<TeacherParcelable?> {
            return arrayOfNulls(size)
        }
    }
}