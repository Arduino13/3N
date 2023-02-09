package com.example.vocab.basic

/**
 * Class that represents teacher
 * @property id identification string
 * @property name name of the teacher
 * @property classes list of class objects that contains students objects
 * @constructor first one is for TeacherParcelable
 * @constructor second one creates a new teacher
 *
 * This class is read-only, because for adding homework or words, there are special methods in database, because
 * otherwise it would be necessary to add every homework to every student object which is complicated
 * and also it'll be non effective
 */
open class Teacher: Syncable{
    var id: String
        protected set
    var name: String
        protected set
    var classes: List<Class>
        protected set

    protected constructor(){
        this.id = ""
        this.name = ""
        this.classes = listOf()
    }

    constructor(id: String,name: String,classes: List<Class>){
        this.id = id
        this.name = name
        this.classes = classes
    }

    /**
     * Returns class based on class id [classID]
     */
    fun getClass(classID: String): Class?{
        for(c in classes){
            if(classID == c.id) return c
        }

        return null
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.Teacher
    }

    override fun getID(): String{
        return id
    }
}