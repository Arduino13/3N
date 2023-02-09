package com.example.vocab.database

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.Settings
import com.example.vocab.DateUtils
import com.example.vocab.filters
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

/**
 * Encodes and decodes dictionaries to JSON message and manages connection to server
 *
 * @property toSend full request with header and content to send to database
 * @property content content of request
 * @property address URL of database server
 * @property userId login credentials
 * @property userHash login credentials
 * @property contextR for getting device id (meant only for debugging, for release it'll be deleted)
 */

class HttpConnector(user: String, hash: String, context: ContentResolver){
    class HttpConnector_exception(message: String) : Exception(message)

    data class Return (var values: MutableMap<String,String>, var sections: MutableMap<String,Return>)

    private var toSend = mutableMapOf<String,String>()
    private var content = mutableMapOf<String,String>()
    private var address: String? = null

    private val userId: String = user
    private val userHash: String = hash
    private val contextR = context

    /**
     * Initializes header of the request
     */
    @SuppressLint("HardwareIds")
    private fun initBuffer(){
        toSend["id"] =  Settings.Secure.getString(contextR, Settings.Secure.ANDROID_ID)
        toSend["time"] = DateUtils.getCurrentDate()
        toSend["version"] = "0.1a"
        toSend["os_v"] = android.os.Build.VERSION.SDK_INT.toString()
        toSend["type"] = android.os.Build.MODEL
        toSend["manu"] = android.os.Build.MANUFACTURER
        toSend["id_user"] = userId
        toSend["hash"] = userHash
    }

    /**
     * Set request type to data and server address to [url]
     */
    fun openData(url: String){
        address = url
        initBuffer()
        content["type"] = "data"
    }

    /**
     * Set request type to message and server address to [url]
     * [msg] will be added to key 'text' inside request
     */
    fun openMessage(url: String, msg: String){
        address=url
        initBuffer()
        content["type"] = "msg"
        content["spec"] = ""
        if(filters.isValid(msg)){
            content["text"] = msg
        }
        else{
            throw HttpConnector_exception("you can not use special characters")
        }
    }

    /**
     * Adds only pair of strings to request
     */
    fun addMessage(data: Map<String,String>){
        for ((key,value) in data){
            content[key] = value
        }
    }

    /**
     * Adds any pair String to Any to request
     *
     * @return encoded JSON data
     */
    private fun addData(data: Map<String,Any>): String{
        var result = mutableMapOf<String,String>()

        for ((key,value) in data){
            if (value is Map<*,*>) result[key] = addData(value as Map<String, Any>)
            else result[key] = value as String
        }

        return Json.encodeToString(result)
    }

    /**
     * Adds any pair String to Any to request
     *
     * @return encoded JSON data
     */
    fun addData(data: Array<Map<String,Any>>){
        for (d in data) {
            for ((key, value) in d) {
                if (key == "type") throw HttpConnector_exception("invalid key")

                if (value is Map<*, *>) content[key] = addData(value as Map<String, Any>)
                else content[key] = value as String
            }
        }
    }

    /**
     * Decodes JSON to pair String to String
     */
    private fun parseInput(data: String): Return?{
       try {
           val decode = Json.decodeFromString<MutableMap<String, String>>(data)
           var toReturn = Return(mutableMapOf<String,String>(), mutableMapOf<String,Return>())
           for((key,value) in decode){
               var subReturn = parseInput(value)
               if(subReturn != null){
                   toReturn.sections[key] = subReturn
               }
               else{
                   toReturn.values[key] = value
               }
           }

           return toReturn
       } catch(e: Exception){
            return null
       }
    }

    /**
     * Sends request to server and waits for response
     *
     * @return retrieved data
     */
    suspend fun sendResponse(): Return?{
        var data: String
        try {
            toSend["content"] = Json.encodeToString(content)
            data = Json.encodeToString(toSend).replace("\\", "")
            data = data.replace("\"{","{")
            data = data.replace("}\"", "}")
        }catch(e: Exception){
            throw HttpConnector_exception("Json error: " + e.localizedMessage)
        }

        //prevzato z: https://stackoverflow.com/questions/35548162/how-to-bypass-ssl-certificate-validation-in-android-app
        val trustAllCerts =
            arrayOf<TrustManager>(object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }

                override fun checkClientTrusted(
                    certs: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun checkServerTrusted(
                    certs: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }
            }
            )

        val sc = SSLContext.getInstance("SSL")
        sc.init(null, trustAllCerts, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        val allHostsValid = HostnameVerifier { hostname, session -> true }
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)

        with(URL(address).openConnection() as HttpURLConnection){
            doOutput = true
            requestMethod = "POST"
            connectTimeout = 500
            readTimeout = 2000

            setRequestProperty("charset", "utf-8")
            //setRequestProperty("Content-Length", data.toCharArray().size.toString())
            setRequestProperty("Content-Type", "application/json")
            //setChunkedStreamingMode(0)

            try {
                outputStream.bufferedWriter().use {
                    it.write(data.toCharArray())
                    it.flush()
                }

                inputStream.bufferedReader().use {
                    return (parseInput(it.readLines().first()))
                }
            }catch(e: Exception){
                throw HttpConnector_exception("Internet error " + e.localizedMessage)
            }
        }
    }
}
