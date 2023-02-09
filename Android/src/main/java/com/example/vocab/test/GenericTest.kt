package com.example.vocab.test

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import androidx.viewbinding.ViewBinding
import com.example.vocab.basic.Word
import java.lang.Exception

/**
 * Class that needs to be inherited by every test
 *
 * @property minWordLength minimal number of letters in each word
 * @property maxWordLength maximal number of letters in each word
 * @property numberOfRequiredWords number of words that need to be in selected translation
 */

abstract class GenericTest<T: ViewBinding>: Fragment() {
    protected lateinit var binding: T
    protected lateinit var words: List<Word>
    private lateinit var onResult: (result: Result) -> Unit
    private lateinit var keyToListen: String

    /**
     * Class for handling results of one test
     */
    data class Result(
        val result: Boolean,
        val correctAnswer: String,
        val testedWord: Word
    )

    abstract val minWordLength: Int
    abstract val maxWordLength: Int
    abstract val numberOfRequiredWords: Int

    /**
     * Evaluates test and returns result
     */
    protected abstract fun checkAndPost(): Result

    fun setArguments(
        keyToListen: String,
        onResult: (result: Result)->Unit
    ): GenericTest<*>{
        this.keyToListen = keyToListen
        this.onResult = onResult
        return this
    }

    fun setWord(words: List<Word>) {
        this.words = words
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if(!this::words.isInitialized||
                !this::keyToListen.isInitialized||
                !this::onResult.isInitialized){
            throw Exception("Object is not initialized properly")
        }
    }

    override fun onResume(){
        super.onResume()

        parentFragmentManager.setFragmentResultListener(keyToListen, requireActivity(),
            FragmentResultListener{ _, _ ->
                val result = checkAndPost()
                onResult(result)
            }
        )
    }
}
