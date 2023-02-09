package com.example.vocab.teacherHome

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Class
import com.example.vocab.basic.Teacher
import com.example.vocab.basic.TeacherParcelable
import com.example.vocab.database.Database
import com.example.vocab.databinding.ActivityTeacherBinding
import com.example.vocab.globalModel
import com.example.vocab.globalModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * Activity which is started when user is teacher
 *
 * @property classesLauncher displays screen with list of teacher's classes
 */
class TeacherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTeacherBinding
    private lateinit var navHost: NavHostFragment

    private val classesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val bundle = data?.getBundleExtra("selectedClass")
            bundle?.let {
                val model: globalModel by viewModels()
                val selectedClassName = bundle["selectedClass"]
                var selectedClass: Class? = null
                for(cls in (model.data.value as Teacher).classes){
                    if(cls.name == selectedClassName){
                        selectedClass = cls
                        break
                    }
                }

                model.selectClass(selectedClass)
                navHost.navController.navigate(R.id.toHomePageTeacher)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Tools.getScreenSizeOfDevice(resources)<6) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }

        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val DB = Database(applicationContext, this)
        val factory = globalModelFactory(DB){
            throw Exception("internal error - inconsistent database")
        }
        val model = ViewModelProvider(this, factory).get(globalModel::class.java)

        if(model.classSelected.value == null) {
            val bottomNav = binding.bottomNavigationTeacher
            navHost =
                supportFragmentManager.findFragmentById(R.id.host_nav_teacher) as NavHostFragment
            NavigationUI.setupWithNavController(bottomNav, navHost.navController)

            val intent = Intent(this, TeacherClassesActivity::class.java)
            intent.putExtra("teacher", TeacherParcelable(model.data.value as Teacher))
            classesLauncher.launch(intent)
        }
    }
}