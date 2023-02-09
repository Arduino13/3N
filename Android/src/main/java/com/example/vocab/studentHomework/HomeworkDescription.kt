package com.example.vocab.studentHomework

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.vocab.*
import com.example.vocab.basic.OpenHomework
import com.example.vocab.basic.Student
import com.example.vocab.basic.StudentParcelable
import com.example.vocab.databinding.FragmentHomeworkDescriptionBinding
import com.example.vocab.homework.BaseHomework
import com.example.vocab.test.TestLaunchUtils
import java.util.Date

/**
 * Fragment of homework description for student
 *
 * @property testLauncher runs a test and saves results
 */
class HomeworkDescription : Fragment() {
    private lateinit var binding: FragmentHomeworkDescriptionBinding
    private lateinit var fragmentModel: HomeWorkModel
    private lateinit var student: Student
    private lateinit var homework: OpenHomework
    private lateinit var id: String

    private val testLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val studentFromTest = data?.getParcelableExtra<StudentParcelable>("student")
                ?: throw java.lang.Exception("internal error during handling parcelable")

            val tests = studentFromTest.listTests.toMutableList()
            val testToCopy = tests.last()

            student = studentFromTest.copy(listWords = student.listWords)
            student.addTest(testToCopy.copy(homeworkID = homework.homework_id))

            val model: globalModel by activityViewModels()
            model.setData(student) //saves test, because that can't be done by homework model

            for(word in testToCopy.words){
                if(!testToCopy.wrongWords.containsFromList(word)){
                    homework.completedWords += word
                }
            }
            if (homework.completedWords.size == homework.wordList.size) homework.completed = true

            val id = fragmentModel.requestAccess()
                ?: throw Exception("access to homeworkModel denied")
            fragmentModel.save(
                BaseHomework(
                    homework
                ), id)
            fragmentModel.commit()
            fragmentModel.releaseAccess()
        }
    }

    /**
     * Runs [testLauncher]
     */
    private fun runTest(){
        if(homework.dateCompleted < Date()) Toast.makeText(
            requireContext(),
            resources.getText(R.string.homework_error_after_date),
            Toast.LENGTH_LONG).show()
        else{
            val homeworkWordList = homework.wordList.toMutableList()
            for(word in homework.completedWords){
                homeworkWordList.removeFrom(word)
            }

            val student = student.copy(listWords = homeworkWordList)

            testLauncher.launch(TestLaunchUtils.getIntent(
                student,
                requireContext(),
                requireActivity(),
                withoutDisableList = true
            ))
        }
    }

    private fun loadData(){
        for(h in fragmentModel.data.value ?: throw Exception("internal error - inconsistent data")){
            if(h.itemID == id){
                binding.homeworkName.text = h.homework.name
                binding.StartDate.text = String.format(
                    resources.getString(R.string.homework_from_date),
                    DateUtils.fromDate2StringShort(h.homework.date)
                )
                binding.EndDate.text = String.format(
                    resources.getString(R.string.homework_to_date),
                    DateUtils.fromDate2StringShort(h.homework.dateCompleted)
                )
                binding.Completed.text = String.format(
                    resources.getString(R.string.homework_completed),
                    if(h.homework.completed) resources.getString(R.string.homework_yes) else resources.getString(R.string.homework_no)
                )
                binding.Completed.setTextColor(
                    if(!h.homework.completed) ResourcesCompat.getColor(resources, R.color.red, null)
                    else ResourcesCompat.getColor(resources, R.color.green, null)
                )

                binding.runTestHomework.setOnClickListener {
                    runTest()
                }

                val homeworkWordList = h.homework.wordList.toMutableList()
                for(word in h.homework.completedWords){
                    homeworkWordList.removeFrom(word)
                }

                childFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.toLearnWordsFragment,
                        HomeworkTableWords.newInstance(
                            homeworkWordList
                        )
                    )
                    replace(
                        R.id.completedWordsFragment,
                        HomeworkTableWords.newInstance(
                            h.homework.completedWords
                        )
                    )
                    commit()
                }

                homework = OpenHomework(h.homework)
                break
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeworkDescriptionBinding.inflate(inflater, container, false)

        val model: globalModel by activityViewModels()
        student = model.data.value as Student

        val _fragmentModel: HomeWorkModel by activityViewModels()
        fragmentModel = _fragmentModel

        id = arguments?.getString("homeworkID") ?: throw Exception("internal error - null homework id")

        fragmentModel.data.observe(viewLifecycleOwner, Observer{
            loadData()
        })

        binding.navBar.setOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        return binding.root
    }
}