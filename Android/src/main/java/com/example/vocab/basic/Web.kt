package com.example.vocab.basic

/**
 * Class that represents web page
 *
 * @property id identifications string
 * @property address web page url address
 * @property class_id same as class_id for student
 */
data class Web(val id: String, val address: String, val name: String, val class_id: String): Syncable{
    override fun getID(): String{
        return id
    }

    override fun getType(): Syncable.Types {
        return Syncable.Types.WebPage
    }
}