package com.example.vocab.gui


import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.database.Database
import com.example.vocab.databinding.ActivityLoginBinding
import com.example.vocab.filters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import kotlin.experimental.and

/**
 * Class that manages login fragment, it's called by test screen in case user is not logged in
 */
class LoginActivity : AppCompatActivity(){
    private lateinit var binding: ActivityLoginBinding

    private fun hashPassword(password: String): String{
        val HEX_CHARS = "0123456789abcdef"
        val bytes = MessageDigest
            .getInstance("SHA-512")
            .digest(password.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }

    private fun login(username: String, password: String): Map<String,Boolean>?{
        val DB = Database(applicationContext, this ,username, hashPassword(password))
        return DB.login()
    }

    /**
     * Displays error message
     */
    private fun showLoginFailed(@StringRes errorString: Int) {
        val appContext = applicationContext
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    /**
     * To display error message when the function is called from coroutine
     */
    private fun loginFailedOnMain(@StringRes errorString: Int){
        CoroutineScope(Dispatchers.Main).launch{
            showLoginFailed(errorString)
            binding.loading.visibility = View.GONE
        }
    }

    fun lockDeviceRotation(value: Boolean) {
        requestedOrientation = if (value) {
            val currentOrientation = resources.configuration.orientation
            if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                ActivityInfo.SCREEN_ORIENTATION_FULL_USER
            } else {
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Tools.getScreenSizeOfDevice(resources)<6) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        else {
            lockDeviceRotation(true)
        }


        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton = binding.login
        val loadingProgressBar = binding.loading
        val passwordLabel = binding.password
        val usernameLabel = binding.username

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE

            val username = usernameLabel.text.toString()
            val password = passwordLabel.text.toString()

            if(filters.isValid(username) && username.isNotEmpty() && password.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    login(username, password)?.let {
                        when {
                            it[Database.DBloginMessage.succes] == true -> {
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            it[Database.DBloginMessage.connectionError] == false -> {
                                loginFailedOnMain(R.string.login_failed)
                            }
                            else -> {
                                loginFailedOnMain(R.string.login_internet_failed)
                            }
                        }
                    } ?: loginFailedOnMain(R.string.login_internet_failed)
                }
            }
            else{
                loginFailedOnMain(R.string.login_failed_invalid_characters)
            }
        }
    }
}