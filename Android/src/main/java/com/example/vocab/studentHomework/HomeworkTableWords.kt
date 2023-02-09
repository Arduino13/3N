package com.example.vocab.studentHomework

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentHomeworkTableWordsBinding
import com.example.vocab.gui.CustomTextView
import com.example.vocab.homework.HomeworkSettingModel

/**
 * Fragment that handles list of words in table
 */
class HomeworkTableWords(): Fragment() {
    private lateinit var binding: FragmentHomeworkTableWordsBinding
    private var words: List<Word>? = null

    companion object{
        /**
         * Creates new instance with list of words
         *
         * Useful when we want to create fragments dynamically
         */
        fun newInstance(words: List<Word>): HomeworkTableWords {
            val newObj = HomeworkTableWords()
            newObj.setArguments(words)

            return newObj
        }
    }

    private fun setArguments(words: List<Word>){
        this.words = words
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeworkTableWordsBinding.inflate(inflater, container, false)

        val settingModel: HomeworkSettingModel by viewModels()
        val words = words ?: settingModel.listOfWords ?: throw Exception("internal exception")
        settingModel.listOfWords = words

        val space = Tools.dp2Pixels(6, resources)

        binding.wordsTable.background = Tools.getCircleLayout(R.color.white, requireContext(), resources)
        binding.wordsTable.setPadding(space, space, space, space)

        var lineLength = 0
        var prevWordBox: CustomTextView? = null
        var prevLineButton: CustomTextView? = null

        for(w in words){
            val wordBox = CustomTextView(requireContext(), w.from)
            wordBox.id = View.generateViewId()

            wordBox.background = Tools.getCircleLayout(R.color.magenta, requireContext(), resources)

            val params = RelativeLayout.LayoutParams(
                wordBox.customWidth,
                wordBox.customHeight
            ).apply {
                leftMargin = space
                bottomMargin = space
                topMargin = space
                rightMargin = space
            }

            lineLength += wordBox.customWidth

            val width = Tools.getScreenWidth(resources) - Tools.dp2Pixels(32, resources)

            prevWordBox?.let{
                if(lineLength <= width) {
                    params.addRule(RelativeLayout.RIGHT_OF, it.id)
                    prevLineButton?.let{
                        params.addRule(RelativeLayout.BELOW, it.id)
                    }
                }
                else{
                    params.addRule(RelativeLayout.BELOW, it.id)
                    lineLength = wordBox.customWidth
                    prevLineButton = prevWordBox
                }
            }

            prevWordBox = wordBox

            binding.wordsTable.addView(wordBox, params)
        }

        return binding.root
    }
}