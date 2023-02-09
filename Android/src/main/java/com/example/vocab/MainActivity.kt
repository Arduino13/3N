package com.example.vocab

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.vocab.database.Database
import com.example.vocab.databinding.ActivityMainBinding
import com.example.vocab.gui.LoginActivity
import com.example.vocab.teacherHome.TeacherActivity
import com.example.vocab.thirdParty.Translator

class MainActivity : AppCompatActivity(){
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHost: NavHostFragment

    private val loginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        tryLogAndDownload(true)
    }

    /**
     * displays login activity
     */
    private fun displayLogin(){
        val intent = Intent(this, LoginActivity::class.java)
        loginLauncher.launch(intent)
    }

    /**
     * tries to log in user and downloads data from server
     */
    private fun tryLogAndDownload(reInit: Boolean = false){
        val DB = Database(applicationContext, this)
        try {
            val loginInfo = DB.login()

            if (loginInfo[Database.DBloginMessage.succes] == true &&
                loginInfo[Database.DBloginMessage.isTeacher] == true
            ) {
                val intent = Intent(this, TeacherActivity::class.java)
                startActivity(intent)

                setResult(Activity.RESULT_OK)
                finish()
                return
            }
        } catch(e: Database.DatabaseException){}

        val factory = globalModelFactory(DB){
            displayLogin()
        }

        val model = ViewModelProvider(this, factory).get(globalModel::class.java)
        if(reInit) model.reInit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        if(Tools.getScreenSizeOfDevice(resources)<6) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        val view = binding.root
        setContentView(view)

        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())

        val appLanguage = Database(applicationContext, this).getSetting(Settings.AppLanguage) as? Int
        if (appLanguage != null){
            Tools.setLocale(this,
                Translator.lang2code(Translator.fromID(appLanguage) ?: Translator.languages.en)
            )
        }

        val bottomNav = binding.bottomNavigation
        navHost = supportFragmentManager.findFragmentById(R.id.host_nav) as NavHostFragment
        NavigationUI.setupWithNavController(bottomNav,navHost.navController)

        tryLogAndDownload()

        /*Translator.downloadRequiredModels { percentage ->
            println(percentage)
        } */

        AppCompatDelegate.setDefaultNightMode(
            if((Database(applicationContext, this).getSetting(Settings.nightMode) as? Boolean) == true)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this, permissions,0)
        }
    }
}