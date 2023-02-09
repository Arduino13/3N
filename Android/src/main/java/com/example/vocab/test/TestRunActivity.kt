package com.example.vocab.test

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.Tools
import com.example.vocab.basic.*
import com.example.vocab.containsFrom
import com.example.vocab.databinding.ActivityTestRunBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*
import kotlin.reflect.KClass

/**
 * Main test fragments which handles test fragment selection, preparing data for them,
 * generating transitions between them and displaying dialogues and results
 */
class TestRunActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestRunBinding
    private lateinit var test: OpenTest
    private lateinit var factory: TestFactoryInterface
    private var maxNumClasses: Int = 0
    private var maxNumReverseClasses: Int = 0

    val keyForButton = "onClickKey"

    private var testedWords: MutableList<OpenWord> = mutableListOf()
    private var lastTested: KClass<out GenericTest<out ViewBinding>>? = null //to prevent running the
                                                                            //same test over and over
    private lateinit var student: Student
    private var numberOfWordsAtBeginning: Int = 0 //used by progress bar

    private var numberOfAttempts = 0
    private var numberOfErrors = 0

    private var reverseWords = false

    /**
     * Updates progress bar
     */
    private fun updateGUI() {
        val temp: Float = testedWords.size.toFloat()/numberOfWordsAtBeginning*100
        binding.progressBarTest.progress = (100-temp).toInt()
    }

    /**
     * Returns results of test back to main activity
     */
    private fun onCloseHandle(state: Boolean = true){
        val data = Intent()
        data.putExtra("student", StudentParcelable(student))
        setResult(if(state) RESULT_OK else RESULT_CANCELED, data)
        finish()
    }

    /**
     * Displays results with [ResultFragment]
     */
    private fun displayResults(){
        binding.closeTest.visibility = View.GONE
        binding.control.visibility = View.GONE

        supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_left_sec,
                R.anim.slide_left,
                R.anim.slide_right,
                R.anim.slide_right_sec
            )
            replace(
                R.id.main,
                ResultFragment.newInstance(
                    100 - (numberOfErrors / numberOfAttempts * 100),
                    numberOfErrors
                ) {
                    student.addTest(test)
                    onCloseHandle()
                })
            commit()
        }
    }

    /**
     * Displays evaluation's results, updates words stats, and delete translations which were answered
     * right
     */
    private fun onResultHandler(result: GenericTest.Result){
        numberOfAttempts += 1

        binding.control.visibility = View.GONE
        val fragmentOverlay = ResultOverlay.newInstance(result){ selfObj ->
            supportFragmentManager.beginTransaction().apply {
                remove(selfObj)
                commit()
            }

            if(testedWords.isNotEmpty()){
                nextTest()
                binding.control.visibility = View.VISIBLE
            }
            else{
                displayResults()
            }
        }

        supportFragmentManager.beginTransaction().apply {
            add(R.id.main, fragmentOverlay)
            commit()
        }

        var testedWord = result.testedWord
        if(reverseWords){
            testedWord = OpenWord(testedWord.copy(from=testedWord.to, to=testedWord.from))
        }

        val wordFromResult = (testedWord as OpenWord)
        wordFromResult.openStats.lastTested = Date()

        var listToRemove = mutableListOf<Word>()
        for(word in testedWords){
            if(word.from == wordFromResult.from){
                listToRemove.add(word)
            }
        }

        testedWords.removeAll(listToRemove)

        if(result.result){
            wordFromResult.openStats.numSucces += 1
        }
        else{
            numberOfErrors +=1

            wordFromResult.openStats.numFail +=1
            if(!test.wrongWords.contains(wordFromResult)) {
                test.wrongWords += wordFromResult
            }
        }

        student.updateWord(wordFromResult)
        updateGUI()
    }

    /**
     * Select next test
     */
    private fun nextTest(animated: Boolean = true){
        var chooseTest: KClass<out GenericTest<out ViewBinding>>

        do{
            chooseTest = if(reverseWords){
                factory.reversableClasses[(0 until maxNumReverseClasses).random()]
            } else {
                factory.classes[(0 until maxNumClasses).random()]
            }
        }while(chooseTest == lastTested)

        lastTested = chooseTest

        val testFragmentObj = factory.getObject(chooseTest, keyForButton){ result ->
            onResultHandler(result)
        }

        if(testFragmentObj.numberOfRequiredWords>testedWords.size){
            nextTest(animated)
            return
        }

        val chooseWords = mutableListOf<Word>()
        for(i in 0 until testFragmentObj.numberOfRequiredWords){
            testedWords.shuffle()
            for(wal in testedWords){
                var w = wal
                if(reverseWords) {
                    w = OpenWord(w.copy(from = w.to, to = w.from))
                }
                if((w.from.split(" ").size >= testFragmentObj.minWordLength || testFragmentObj.minWordLength == -1)
                    && (w.from.split(" ").size <= testFragmentObj.maxWordLength || testFragmentObj.maxWordLength == -1)
                    && !chooseWords.containsFrom(w)){
                    chooseWords.add(w)
                    break
                }
            }
        }

        if(chooseWords.size < testFragmentObj.numberOfRequiredWords){
            nextTest(animated)
            return
        }

        testFragmentObj.setWord(chooseWords)

        if(animated){
            supportFragmentManager.beginTransaction().apply {
                setCustomAnimations(
                    R.anim.slide_left_sec,
                    R.anim.slide_left,
                    R.anim.slide_right,
                    R.anim.slide_right_sec
                )
                replace(R.id.testFragmentContainer, testFragmentObj)
                commit()
            }
        }
        else{
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.testFragmentContainer, testFragmentObj)
                commit()
            }
        }
    }

    /**
     * Rotating of screen caused problems
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Tools.getScreenSizeOfDevice(resources)<6) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        else {
            lockDeviceRotation(true)
        }

        binding = ActivityTestRunBinding.inflate(layoutInflater)
        setContentView(binding.root)

        student = intent.getParcelableExtra<StudentParcelable>("student")
            ?: throw Exception("internal error during handling parcelable")
        val maxNumOfWords = intent.getIntExtra(Settings.maxNumWords, Settings.maxNumWordsDef)
        reverseWords = intent.getBooleanExtra(Settings.reverseTests, Settings.reverseTestsDef)
        val disabledListString = intent.getStringExtra(Settings.disabledWordLists) ?: ""

        val disabledList = disabledListString.split(',')

        test = TestGenerator.newTest(student.listWords, maxNumOfWords, disabledList)
            ?: throw Exception("internal error during generating test")
        factory = TestFragmentFactory()

        maxNumClasses = factory.classes.size
        maxNumReverseClasses = factory.reversableClasses.size
        for(w in test.words){
            testedWords.add(OpenWord(w))
        }

        if(test.words.isEmpty()) throw Exception("empty list of words")
        numberOfWordsAtBeginning = test.words.size

        binding.control.setOnClickListener {
            supportFragmentManager.setFragmentResult(keyForButton, bundleOf())
        }
        binding.closeTest.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(resources.getString(R.string.test_close_dialog_title))
                .setMessage(resources.getString(R.string.test_close_dialog_question))
                .setNegativeButton(resources.getString(R.string.button_base_negative_no)) { _,_ -> }
                .setPositiveButton(resources.getString(R.string.button_base_positive)) { _,_ ->
                    onCloseHandle(state = false)
                }
                .show()
        }

        nextTest(false)
    }
}