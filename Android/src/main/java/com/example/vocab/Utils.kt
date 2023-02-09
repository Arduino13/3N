package com.example.vocab

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Patterns
import android.util.TypedValue
import android.webkit.URLUtil
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.vocab.basic.Word
import com.example.vocab.thirdParty.Translator
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class Settings{
    companion object{
        const val login = "login_creditals"
        const val password = "password_creditals"
        const val teacher = "teacher_creditals"
        const val rssNum = "max_number_articles"
        const val nightMode = "night_mode"
        const val language = "language"
        const val maxNumWords = "max_num_words"
        const val reverseTests = "reverse_tests"
        const val disabledWordLists = "wordListsDisabled"
        const val AppLanguage = "app_lang"

        const val rssNumDef = 15
        const val nightModeDef = false
        val languageDef = Translator.languages.en
        const val maxNumWordsDef = 20
        const val reverseTestsDef = false
    }
}

class Tools{
    companion object{
        fun isVertical(resources: Resources): Boolean{
            return resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE
        }
        fun getUUID(): String{
            return UUID.randomUUID().toString()
        }
        fun dp2Pixels(dps: Int, resources: Resources): Int{
            val scale = resources.displayMetrics.density
            return (dps * scale + 0.5).toInt()
        }
        fun spToPx(sp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context.resources.displayMetrics
            ).toInt()
        }
        fun getCircleLayout(@ColorRes color: Int, context: Context, resources: Resources): Drawable?{
            val shape = ContextCompat.getDrawable(context, R.drawable.layout_circle)
            shape?.colorFilter = PorterDuffColorFilter(ResourcesCompat.getColor(resources, color, null), PorterDuff.Mode.OVERLAY)

            return shape
        }

        fun getScreenWidth(resources: Resources): Int {
            val dm = resources.displayMetrics
            return dm.widthPixels
        }

        fun getScreenHeight(resources: Resources): Int {
            val dm = resources.displayMetrics
            return dm.heightPixels
        }

        fun getScreenSizeOfDevice(resources: Resources): Double {
            val dm = resources.displayMetrics
            val width = dm.widthPixels
            val height = dm.heightPixels
            val x = Math.pow(width.toDouble(), 2.0)
            val y = Math.pow(height.toDouble(), 2.0)
            val diagonal = Math.sqrt(x + y)
            val dens = dm.densityDpi
            val screenInches = diagonal / dens.toDouble()

            return screenInches
        }

        fun setLocale(activity: Activity, languageCode: String?) {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            val resources = activity.resources
            val config = resources.configuration
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)
        }
    }
}

class AssetsUtils{
    companion object {
        fun assetFilePath(context: Context, asset: String): String {
            val file = File(context.filesDir, asset)

            try {
                val inpStream: InputStream = context.assets.open(asset)
                try {
                    val outStream = FileOutputStream(file, false)
                    val buffer = ByteArray(4 * 1024)
                    var read: Int

                    while (true) {
                        read = inpStream.read(buffer)
                        if (read == -1) {
                            break
                        }
                        outStream.write(buffer, 0, read)
                    }
                    outStream.flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return ""
        }
    }
}

class DateUtils{
    companion object {
        fun getCurrentDate(): String{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                var answer: String =  current.format(formatter)
                return answer
            } else {
                var date = Date()
                val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
                val answer: String = formatter.format(date)
                return answer
            }
        }

        fun fromString(date: String): Date{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val date = LocalDateTime.parse(date,formatter)
                return Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
            } else {
                val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
                return formatter.parse(date)
            }
        }

        fun fromDate2String(date: Date, format: String = "yyyy-MM-dd HH:mm"): String{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val current = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern(format)
                var answer: String =  current.format(formatter)
                return answer
            } else {
                val formatter = SimpleDateFormat(format)
                val answer: String = formatter.format(date)
                return answer
            }
        }

        fun fromDate2StringShort(date: Date): String{
            return fromDate2String(date, format="MM.dd")
        }
    }
}

class VocabularyUtils{
    companion object{
        const val defaultListName = "Výchozí"

        /**
         * separates list's name from word
         */
        fun separateList(source: String): String{
            val nameIndex = source.indexOf("#")

            if(nameIndex != -1){
                return source.substring(nameIndex+1)
            }
            else{
                return defaultListName
            }
        }

        /**
         * adds list's name to word
         */
        fun addList(source: Word): String{
            return if(source.list == defaultListName){
                source.from
            } else{
                source.from + "#" + source.list
            }
        }

        /**
         * separates word from string with list's name
         */
        fun separateWord(source: String): String{
            val nameIndex = source.indexOf("#")

            if(nameIndex != -1){
                return source.substring(0,nameIndex)
            }

            return source
        }

        /**
         * converts list of words to string with @ as separator between original word and it's
         * translation and uses ; as separator between translations
         */
        fun fromArrayToString(source: List<Word>): String{
            var tempArray = arrayOf<String>()

            for(word in source){
                tempArray += addList(word) + "@" + word.to
            }

            return tempArray.joinToString(";")
        }

        /**
         * coverts string to array see [fromArrayToString]
         */
        fun fromStringToArray(source: String): List<Word>{
            var arrayOfWords = source.split(";")

            if(arrayOfWords[0] == ""){
                return listOf<Word>()
            }

            var toReturn = listOf<Word>()
            for(word in arrayOfWords){
                val splited = word.split("@")
                if(splited.count() > 1) {
                    toReturn += Word(Tools.getUUID(), separateWord(word.split("@")[0]), word.split("@")[1], list=separateList(word.split("@")[0]))
                }
                else if(splited.count() == 1){
                    toReturn += Word(Tools.getUUID(), separateWord(word.split("@")[0]), "", list=separateList(word.split("@")[0]))
                }
            }

            return toReturn
        }
    }
}

//Some useful methods extension that are used within project
fun <K,V> MutableMap<K, MutableList<V>>.merge(key: K, value: MutableList<V>){
    if(key in this){
        this[key]?.addAll(value)
    }
    else{
        this[key] = value
    }
}

fun <K,V> MutableMap<K, MutableMap<V,V>>.merge(key: K, value: MutableMap<V,V>){
    if(!this.contains(key)){
        this[key] = value
    }
    else{
        for((keyS,valueS) in value){
            this[key]!![keyS] = valueS
        }
    }
}

fun List<Word>.indexFromList(key: Word): Word?{
    for(word in this){
        if(word.from == key.from) return word
    }

    return null
}

fun List<Word>.containsFromList(key: Word): Boolean{
    var result = false

    for(word in this){
        if(word.from == key.from){
            result = true
            break
        }
    }

    return result
}

fun MutableList<Word>.containsFrom(key: Word): Boolean{
    var result = false

    for(word in this){
        if(word.from == key.from){
            result = true
            break
        }
    }

    return result
}

fun MutableList<Word>.indexFrom(key: Word): Word?{
    for(word in this){
        if(word.from == key.from) return word
    }

    return null
}

fun MutableList<Word>.removeFrom(key: Word){
    var toRemove: Word? = null
    for(word in this){
        if(word.from == key.from){
            toRemove = word
            break
        }
    }

    toRemove?.let{
        remove(toRemove)
    }
}

class filters{
    companion object{
        const val otherCharacters = "ěščřžýáíéďťň"

        /**
         * test if string is valid as word
         */
        fun isValid(msg: String): Boolean{
            val len = msg.filter { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it in "," || it in "!"
                    || it in "?" || it in "," || it in " "  || it in otherCharacters || it in otherCharacters.toUpperCase()}.length
            return msg.length == len && msg.isNotEmpty()
        }
        fun validAddress(urlString: String): Boolean{
            try {
                val url = URL(urlString)
                return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString)
                    .matches()
            } catch (ignored: MalformedURLException) {}

            return false
        }
        fun containsHttp(address: String): Boolean{
            return address.contains("https://") || address.contains("http://")
        }
    }
}