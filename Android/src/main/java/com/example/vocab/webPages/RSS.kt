package com.example.vocab.webPages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

/**
 * Class for RSS processing, i know there are libraries for that but i wanted to write my own just for
 * practice
 */
class RSS(val maxArticleCount: Int){
    private var testResult = false

    data class Article(val header: String, val context: String, val picture: Bitmap?, val webLink: String){
        companion object{
            const val xmlHeader = "title"
            const val xmlContext = "description"
            const val xmlWebLink = "link"
        }
    }

    private suspend fun loadSite(address: String): InputStream?{
        try{
            with(URL(address).openConnection() as HttpURLConnection){
                requestMethod = "GET"
                setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ")
                setRequestProperty("Accept","*/*")
                connectTimeout = 4000

                var redirect = false

                val status: Int = responseCode
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) redirect = true
                }

                if (redirect) {
                    val newUrl: String = getHeaderField("Location")
                    return loadSite(newUrl)
                }

                return inputStream
            }
        }catch(e: IOException){
            return null
        }
    }

    private suspend fun downloadImage(link: String): Bitmap?{
        loadSite(link)?.let {
            return BitmapFactory.decodeStream(it)
        } ?: return null
    }

    private suspend fun parseXml(stream: InputStream, doNotParseImages: Boolean = false): List<RSS.Article>?{
        var parserFactory = SAXParserFactory.newInstance()
        var parser = parserFactory.newSAXParser()

        var articles = mutableListOf<Article>()
        var header: String? = null
        var context: String? = null
        var picture: Bitmap? = null
        var webLink: String? = null
        var currentElement: String = ""
        var isItem = false
        var errorFlag = false
        var ignoreFlag = false

        try {
            var handler: DefaultHandler = object : DefaultHandler() {
                @Throws(SAXException::class)
                override fun startElement(
                    uri: String?,
                    localName: String?,
                    qName: String?,
                    attributes: Attributes?
                ) {
                    if (!ignoreFlag) {
                        if (localName == "item") {
                            isItem = true
                        } else if ((localName == RSS.Article.xmlHeader || localName == RSS.Article.xmlContext || localName == RSS.Article.xmlWebLink) && isItem) {
                            currentElement = localName
                        } else if (isItem) {
                            attributes?.let {
                                for (i in 0..attributes.length) {
                                    val data = attributes.getValue(i)
                                    if (data != null && (data.contains(".jpg") || data.contains(".JPG") || data.contains(
                                            ".jpeg"
                                        ) || data.contains(".png"))
                                    ) {
                                        if(!doNotParseImages) {
                                            runBlocking {
                                                picture = downloadImage(data)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Throws(SAXException::class)
                override fun characters(ch: CharArray?, start: Int, length: Int) {
                    if (isItem && ch != null) {
                        val string = String(ch, start, length)

                        if (currentElement == RSS.Article.xmlHeader) {
                            header = string
                        } else if (currentElement == RSS.Article.xmlWebLink) {
                            webLink = string
                        } else if (currentElement == RSS.Article.xmlContext) {
                            context = string
                        } else if (string.contains(".jpg") || string.contains(".JPG") || string.contains(".jpeg") || string.contains(".png")) {
                            if(!doNotParseImages) {
                                runBlocking {
                                    picture = downloadImage(string)
                                }
                            }
                        }
                    }
                }

                @Throws(SAXException::class)
                override fun endElement(uri: String?, localName: String?, qName: String?) {
                    if (isItem && localName == "item") {
                        isItem = false
                        if (header != null && context != null && webLink != null) {
                            articles.add(RSS.Article(header!!, context!!, picture, webLink!!))

                            header = null
                            context = null
                            webLink = null
                            picture = null
                            if (articles.count() > maxArticleCount) {
                                ignoreFlag = true
                            }
                        } else {
                            errorFlag = true
                        }
                    } else if (currentElement != "") {
                        currentElement = ""
                    }
                }
            }

            parser.parse(stream, handler)
        } catch (e: IOException) {
            return null
        } catch (e: ParserConfigurationException) {
            return null
        } catch (e: SAXException) {
            return null
        }

        return articles
    }

    private suspend fun loadFeedP(address: String,
                                 completionHandler: ((Boolean, List<RSS.Article>?) -> Unit)?,
                                 doNotParseImages: Boolean = false){
        loadSite(address)?.let {
            val result = parseXml(it, doNotParseImages)
            if (result != null) {
                completionHandler?.let {
                    completionHandler(true, result)
                }
            } else {
                completionHandler?.let {
                    completionHandler(false, null)
                }
            }
        } ?: run{ completionHandler?.let { it(false,null) } }
    }

    fun loadFeed(address: String, completionHandler: ((Boolean, List<Article>?) -> Unit)?){
        CoroutineScope(Dispatchers.IO).launch {
            loadFeedP(address, completionHandler)
        }
    }

    /**
     * testing when saving new page RSS if it's really RSS
     */
    fun testIsRSS(address: String): Boolean{
        var result = false
        runBlocking {
            loadFeedP(address, { succes, _ -> result = succes}, true)
        }

        return result
    }
}