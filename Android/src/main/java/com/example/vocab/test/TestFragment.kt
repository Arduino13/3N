package com.example.vocab.test

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.basic.Student
import com.example.vocab.basic.StudentParcelable
import com.example.vocab.database.Database
import com.example.vocab.databinding.TestFragmentBinding
import com.example.vocab.globalModel
import java.lang.Exception

/**
 * Main fragment which is displayed when student's part of application starts
 */
class TestFragment : Fragment() {
    private lateinit var binding: TestFragmentBinding
    private lateinit var fragmentModel: globalModel

    companion object {
        fun newInstance() = TestFragment()
    }

    private val testLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK || result.resultCode == Activity.RESULT_CANCELED) {
            val data: Intent? = result.data
            val student = data?.getParcelableExtra<StudentParcelable>("student")

            student?.let {
                fragmentModel.setData(student)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TestFragmentBinding.inflate(inflater, container, false)

        val globalModel: globalModel by activityViewModels()
        fragmentModel = globalModel

        binding.runTest.setOnClickListener {
            if((fragmentModel.data.value as Student).listWords.size == 0){
                Toast.makeText(
                    requireContext(),
                    resources.getText(R.string.test_no_words),
                    Toast.LENGTH_LONG).show()
            }
            else {
                testLauncher.launch(
                    TestLaunchUtils.getIntent(
                        fragmentModel.data.value as Student,
                        requireContext(),
                        requireActivity()
                    )
                )
            }
        }

        binding.testLists.setOnClickListener {
            binding.root.findNavController().navigate(R.id.toTestList)
        }

        binding.rankList.setOnClickListener {
            binding.root.findNavController().navigate(R.id.toRankList)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.root.addOnLayoutChangeListener{ view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int ->
            val anim = AnimationUtils.loadAnimation(this.context, R.anim.scale)
            anim.repeatCount = Animation.INFINITE
            binding.imageView.startAnimation(anim)
        }
    }

}