package com.example.vocab.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.get
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentTestChooseFourBinding
import com.example.vocab.databinding.FragmentTestIsCorrectBinding

/**
 * Student select if given translation is correct
 */
class TestIsCorrectFragment : GenericTest<FragmentTestIsCorrectBinding>() {
    override val maxWordLength: Int = -1
    override val minWordLength: Int = -1
    override val numberOfRequiredWords: Int = 2

    private lateinit var testedWord: Word
    private var answer: Boolean? = null
    private var correctAnswer: Boolean? = null

    override fun checkAndPost(): Result{
        return if(answer == correctAnswer){
            Result(true,
                if(correctAnswer!!) binding.yes.text.toString() else binding.no.text.toString(),
                testedWord)
        } else{
            Result(false,
                if(correctAnswer!!) binding.yes.text.toString() else binding.no.text.toString(),
                testedWord)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestIsCorrectBinding.inflate(inflater, container, false)

        val randomValue = (0..1).random()
        correctAnswer = randomValue == 1

        binding.fromWord.text = words[0].from
        testedWord = words[0]

        if(correctAnswer!!){
            binding.toWord.text = words[0].to
        }
        else{
            binding.toWord.text = words[1].to
        }

        binding.background.background = Tools.getCircleLayout(R.color.gray, requireContext(), resources)
        binding.yes.setOnClickListener {
            answer = true
            binding.yes.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.darkGreen, null))
            binding.no.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.red, null))
        }
        binding.no.setOnClickListener {
            answer = false
            binding.yes.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.green, null))
            binding.no.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.darkRed, null))
        }

        return binding.root
    }
}