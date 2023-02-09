package com.example.vocab.gui

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.example.vocab.R
import java.lang.Exception
import java.util.*

/**
 * Class for generating "random" colors
 */
class Colors {
    enum class colors(val value: Int){
        BROWN(0),
        BLUE(1),
        CYAN(2),
        GREEN(3),
        MAGENTA(4),
        RED(5),
        TEAL(6),
        YELLOW(7)
    }
    companion object{
        private val generatedColors = mutableListOf<colors>()

        private val map = colors.values().associateBy(colors::value)
        fun fromInt(type: Int) = map[type] ?: throw Exception()

        fun generateColor(context: Resources): Int? {
            val randomGenerator = Random()
            var color: colors
            do{
                color=fromInt(randomGenerator.nextInt(7))
            }while(generatedColors.contains(color) || color == colors.BLUE)

            if(generatedColors.size>4){
                generatedColors.removeAt(0)
                generatedColors += color
            }
            else{
                generatedColors += color
            }

            return when(color){
                colors.BROWN -> ResourcesCompat.getColor(context, R.color.brown, null)
                colors.BLUE -> ResourcesCompat.getColor(context, R.color.blue, null)
                colors.CYAN -> ResourcesCompat.getColor(context, R.color.cyan, null)
                colors.GREEN -> ResourcesCompat.getColor(context, R.color.green, null)
                colors.MAGENTA -> ResourcesCompat.getColor(context, R.color.magenta, null)
                colors.RED -> ResourcesCompat.getColor(context, R.color.red, null)
                colors.YELLOW -> ResourcesCompat.getColor(context, R.color.yellow, null)
                colors.TEAL -> ResourcesCompat.getColor(context, R.color.teal, null)
                else -> null
            }
        }
    }
}