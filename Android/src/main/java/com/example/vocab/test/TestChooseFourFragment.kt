package com.example.vocab.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentTestChooseFourBinding

/**
 * Displays four translations which only one is right
 */

class TestChooseFourFragment : GenericTest<FragmentTestChooseFourBinding>() {
    override val maxWordLength: Int = 1
    override val minWordLength: Int = -1
    override val numberOfRequiredWords: Int = 4

    private lateinit var chooseLabel: CheckBox
    private lateinit var chooseWord: Word

    override fun checkAndPost(): Result{
        return if(chooseLabel.isChecked){
            Result(true, chooseLabel.text.toString(), chooseWord)
        } else{
            Result(false, chooseLabel.text.toString(), chooseWord)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestChooseFourBinding.inflate(inflater, container, false)

        val randomValue = (0 until 4).random()
        val arrayOfCheckBoxes = listOf(
            binding.answer1,
            binding.answer2,
            binding.answer3,
            binding.answer4
        )

        for(i in 0 until 4){
            arrayOfCheckBoxes[i].text = words[i].to
            arrayOfCheckBoxes[i].background = Tools.getCircleLayout(R.color.gray, requireContext(), resources)
            arrayOfCheckBoxes[i].setPadding(
                Tools.dp2Pixels(20, resources),
                Tools.dp2Pixels(2, resources),
                Tools.dp2Pixels(20, resources),
                Tools.dp2Pixels(2, resources)
            )
            arrayOfCheckBoxes[i].setOnClickListener {
                for(k in 0 until 4){
                    val box = arrayOfCheckBoxes[k]
                    if(box != arrayOfCheckBoxes[i]){
                        box.isChecked = false
                    }
                }
            }
        }

        chooseLabel = arrayOfCheckBoxes[randomValue]
        chooseWord = words[randomValue]
        binding.from.text = words[randomValue].from

        return binding.root
    }
}