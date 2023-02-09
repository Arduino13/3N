package com.example.vocab.teacherHome

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Teacher
import com.example.vocab.basic.TeacherParcelable
import com.example.vocab.database.Database
import com.example.vocab.databinding.ActivityTeacherClassesBinding
import com.example.vocab.gui.*

/**
 * Displays teacher's classes
 */

class TeacherClassesActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var binding: ActivityTeacherClassesBinding
    private var screenSize = 0.0

    /**
     * Generates round corners and sets background to 'random' color
     */
    private fun getShape(cls: BaseTextCell): Drawable?{
        val db = Database(applicationContext, this)

        val shape = ContextCompat.getDrawable(applicationContext, R.drawable.layout_circle)
        val shapeColor = (db.getSetting("${cls.text}##class") as? Int) ?: run {
            val generatedColor = Colors.generateColor(resources)!!
            db.saveSetting(
                mapOf<String,Int>(
                    "${cls.text}##class" to generatedColor
                )
            )
            generatedColor
        }
        shape?.colorFilter = PorterDuffColorFilter(shapeColor, PorterDuff.Mode.OVERLAY)

        return shape
    }

    /**
     * Generates list of [BaseHeader]
     */
    private fun setUpList(teacher: Teacher): List<BaseItem<*>>{
        val toReturn = mutableListOf<BaseItem<*>>()
        val height = if (screenSize>6) Tools.dp2Pixels(80, resources) else Tools.dp2Pixels(40, resources)

        toReturn.add(
            BaseHeader(
                resources.getString(R.string.choose_class),
                height + 30
            )
        )

        for(cls in teacher.classes){
            val cell = BaseTextCell(cls.name)

            if (screenSize > 6) {
                cell.height = height
                cell.alignment = BaseTextCell.wordAlign.center
                cell.layoutShape = getShape(cell)
            }
            else{
                cell.height = height
                cell.alignment = BaseTextCell.wordAlign.left
                cell.layoutShape = getShape(cell)
            }

            toReturn.add(cell)
        }

        return toReturn
    }

    /**
     * Prevents screen from rotating
     */
    private fun lockDeviceRotation(value: Boolean) {
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Tools.getScreenSizeOfDevice(resources)<6) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        else {
            lockDeviceRotation(true)
        }

        binding = ActivityTeacherClassesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val view = binding.listClasses

        val adapterT = BaseListAdapter()
        val teacher = intent.getParcelableExtra<TeacherParcelable>("teacher")
            ?: throw Exception("internal error during handling parcelable - teacher")

        screenSize = Tools.getScreenSizeOfDevice(resources)

        ResponsibleList.makeResponsible(view, resources, applicationContext)
        view.adapter = adapterT

        adapterT.submitList(setUpList(teacher))

        val mDetector = GestureDetectorCompat(applicationContext, this)
        mDetector.setIsLongpressEnabled(true)
        binding.listClasses.setOnTouchListener { _, motionEvent ->
            mDetector.onTouchEvent(motionEvent)
        }
    }

    override fun onShowPress(p0: MotionEvent?) {}

    /**
     * Buttons cause troubles with displaying in right layout shape, so instead text view is used,
     * but clicks need to be handled by fragment/activity
     */
    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        p0?.let {
            val headerText = binding.listClasses.findChildViewUnder(it.x, it.y)?.findViewById<TextView>(R.id.textView)?.text
            headerText?.let {
                val data = Intent()
                data.putExtra("selectedClass", bundleOf("selectedClass" to headerText))
                setResult(Activity.RESULT_OK, data)
                finish()
            }
        }

        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean = false

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) {}
}