package com.example.vocab.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentResultListener
import com.example.vocab.R
import com.example.vocab.basic.OpenTest
import com.example.vocab.basic.OpenWord
import com.example.vocab.basic.Test
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentTestDialogBinding

/**
 * Test's ability to write full translation
 */

class TestDialogFragment : GenericTest<FragmentTestDialogBinding>(){
    override val maxWordLength: Int = 3
    override val minWordLength: Int = -1
    override val numberOfRequiredWords: Int = 1

    private lateinit var word: Word

    override fun checkAndPost(): Result{
        return if(binding.answer.text.toString().toLowerCase() == word.from.toLowerCase()){
            Result(true, word.from, word)
        } else{
            Result(false, word.from, word)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestDialogBinding.inflate(inflater, container, false)
        word = words.first()

        binding.fromWord.text = word.to

        return binding.root
    }
}