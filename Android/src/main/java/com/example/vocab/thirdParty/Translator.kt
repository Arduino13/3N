package com.example.vocab.thirdParty

import android.app.Activity
import android.content.res.Resources
import com.example.vocab.R

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Class for translating text online, offline method is for now turned off see [downloadRequiredModels]
 */
class Translator {
    enum class languages(val language: Int){
        en(R.string.translator_en),
        cs(R.string.translator_cz),
        de(R.string.translator_de),
        es(R.string.translator_es),
        it(R.string.translator_it),
        sl(R.string.translator_sk)
    }

    companion object{
        private val map = Translator.languages.values().associateBy(Translator.languages::language)
        private val languagesToCode = mapOf<languages, String>(
            languages.en to "en",
            languages.cs to "cs",
            languages.es to "es",
            languages.it to "it",
            languages.sl to "sk"
        )

        fun fromID(type: Int) = map[type]
        fun lang2code(type: languages) = languagesToCode[type]

        private val languagesToGoogle = mapOf<languages, String>(
            languages.en to TranslateLanguage.ENGLISH,
            languages.de to TranslateLanguage.GERMAN,
            languages.es to TranslateLanguage.SPANISH,
            languages.it to TranslateLanguage.ITALIAN,
            languages.sl to TranslateLanguage.SLOVAK
        )

        /**
         * Helps load webpage's content
         */
        private fun loadSite(address: String): InputStream?{
            try {
                with(URL(address).openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ")
                    setRequestProperty("Accept", "*/*")
                    connectTimeout = 4000

                    var redirect = false

                    val status: Int = responseCode
                    if (status != HttpURLConnection.HTTP_OK) {
                        if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) redirect =
                            true
                    }

                    if (redirect) {
                        val newUrl: String = getHeaderField("Location")
                        return loadSite(newUrl)
                    }

                    return inputStream
                }
            }catch(e: Exception){
                return null
            }
        }

        fun translate(from: languages, to: languages, wordToTranslate: String, onResult: (String)->Unit) {
            val encodedWord = URLEncoder.encode(wordToTranslate, "utf-8")

            //I know this isn't a right way to obtain translation from google but for small personal
            // project it was good enough
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=${from.name}&tl=${to.name}&dt=t&q=$encodedWord"

            val input = loadSite(url)
            var sub = ""

            input?.let {
                val reader = BufferedReader(it.reader())
                var content: String
                reader.use { reader ->
                    content = reader.readText()
                }

                val fromIndex = content.indexOf("\"")
                val toIndex = content.indexOf("\"", fromIndex + 1)

                sub = content.substring(fromIndex + 1, toIndex)
            } ?: run{
                if(to in languagesToGoogle){
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(languagesToGoogle.getValue(from))
                        .setTargetLanguage(languagesToGoogle.getValue(to))
                        .build()
                    val translator = Translation.getClient(options)

                    translator.translate(wordToTranslate)
                        .addOnSuccessListener { text ->
                            sub = text
                        }
                }
            }

            onResult(sub)
        }

        fun downloadRequiredModels(progress: (Int)->Unit){
            val conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()

            val maxNum = languagesToGoogle.keys.size
            var currentlyDownload = 0

            for(lang in languagesToGoogle.values.toList()){
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(lang)
                    .setTargetLanguage(TranslateLanguage.CZECH)
                    .build()
                val translator = Translation.getClient(options)

                translator.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        progress(currentlyDownload/maxNum*100)
                    }
                    .addOnFailureListener { throw Exception("external error - can't download all languages models") }

                currentlyDownload += 1
            }
        }
    }
}