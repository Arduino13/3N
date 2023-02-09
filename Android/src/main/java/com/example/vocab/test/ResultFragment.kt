package com.example.vocab.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.databinding.FragmentResultBinding

/**
 * Fragment with test results
 */

class ResultFragment : Fragment() {
    private lateinit var binding: FragmentResultBinding

    private var successRation = 0
    private var numberOfErrors = 0
    private lateinit var callBack: () -> Unit

    companion object{
        /**
         * Displays fragment, [callBack] servers purpose to notify about closing of results fragment
         */
        fun newInstance(successRation: Int, numberOfErrors: Int, callBack: ()->Unit): ResultFragment{
            val newObj = ResultFragment()
            newObj.setArguments(successRation, numberOfErrors, callBack)

            return newObj
        }
    }

    private fun setArguments(successRation: Int, numberOfErrors: Int, callBack: () -> Unit){
        this.successRation = successRation
        this.numberOfErrors = numberOfErrors
        this.callBack = callBack
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultBinding.inflate(inflater, container, false)

        if(successRation > 60) binding.successRation.setTextColor(ResourcesCompat.getColor(resources, R.color.green, null))
        else binding.successRation.setTextColor(ResourcesCompat.getColor(resources, R.color.red, null))

        binding.successRation.text = String.format(
            resources.getString(R.string.test_result_success_rate),
            successRation
        )
        binding.errorCount.text = String.format(
            resources.getString(R.string.test_result_wrong_count),
            numberOfErrors
        )

        binding.buttonContinue.setOnClickListener {
            callBack()
        }

        return binding.root
    }
}