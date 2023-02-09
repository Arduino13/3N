package com.example.vocab.test

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationSet
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentTestSentenceBinding
import com.example.vocab.gui.CustomTextView
import kotlinx.android.synthetic.main.fragment_setting.view.*
import kotlinx.android.synthetic.main.fragment_test_choose_four.*
import kotlinx.android.synthetic.main.fragment_web_list_items_cell.view.*

/**
 * Tests ability to put words into the sentence in right order
 */
class TestSentenceFragment : GenericTest<FragmentTestSentenceBinding>() {
    override val maxWordLength: Int = 10
    override val minWordLength: Int = 3
    override val numberOfRequiredWords: Int = 1

    private lateinit var answerContainer: WordsContainer
    private lateinit var wordsContainer: WordsContainer

    /**
     * Encapsulates word's boxes and returns absolute coordinate to fragment
     *
     * @property rootCoordinates upper left corder of container
     * @property flexible defines container behavior when word is removed, if it's true then all
     * words will have maximal space between them defined by [spaceBetweenWords], otherwise there will
     * be space created by removed word
     */
    private class WordsContainer(
        private val rootCoordinates: Pair<Int, Int>,
        private var width: Int,
        private val lineHeight: Int,
        private val spaceBetweenWords: Int,
        private val flexible: Boolean
    ){
        init{
            width += rootCoordinates.first
        }

        val wordsByPosition: List<String>
            get(){
                val words = mutableListOf<String>()
                for(word in wordPositions.keys.toList()){
                    words.add(word.text)
                }

                return words
            }

        private val wordPositions = LinkedHashMap<CustomTextView, Pair<Int, Int>>()
        private var endPointer = rootCoordinates

        /**
         * adds word at the end if the word is new or to the same position where it was
         */
        fun addWord(word: CustomTextView): Map<CustomTextView, Pair<Int,Int>>{
            return if(wordPositions.containsKey(word) && !flexible){
                mapOf(word to wordPositions[word]!!)
            } else{
                addWordAtTheEnd(word)
            }
        }

        /**
         * adds word at the end
         */
        private fun addWordAtTheEnd(word: CustomTextView): Map<CustomTextView, Pair<Int, Int>>{
            endPointer = when {
                wordPositions.size == 0 -> {
                    Pair(rootCoordinates.first + (2*spaceBetweenWords) + word.customWidth,
                        endPointer.second)
                }
                (endPointer.first + spaceBetweenWords + word.customWidth) > width -> {
                    Pair(rootCoordinates.first + (2*spaceBetweenWords) + word.customWidth,
                        endPointer.second + lineHeight)
                }
                else -> {
                    Pair(endPointer.first + spaceBetweenWords + word.customWidth,
                        endPointer.second)
                }
            }

            val position =  mapOf(word to Pair(endPointer.first - spaceBetweenWords - word.customWidth,
                endPointer.second))
            wordPositions += position
            return position
        }

        /**
         * deletes word form position
         */
        fun removeWord(word: CustomTextView): Map<CustomTextView, Pair<Int,Int>>{
            if(flexible){
                var deleteFlag = false
                var finalEndPosition = rootCoordinates
                val toRemove = mutableListOf<CustomTextView>()
                for(i in 0 until wordPositions.size){
                    val item = wordPositions.keys.toList()[i]
                    if(item == word || deleteFlag){
                        toRemove += item
                        deleteFlag = true
                    }
                    else{
                        finalEndPosition = Pair(
                            item.x.toInt() + item.customWidth + spaceBetweenWords,
                            item.y.toInt()
                        )
                    }
                }

                for(k in toRemove){
                    wordPositions.remove(k)
                }
                endPointer = finalEndPosition

                val toReturn = mutableMapOf<CustomTextView, Pair<Int,Int>>()
                for(i in 1 until toRemove.size){
                    val view = toRemove[i]
                    toReturn[view] = addWordAtTheEnd(view).getValue(view)
                }

                return toReturn
            }

            else{
                return mapOf()
            }
        }
    }

    override fun checkAndPost(): Result {
        val answer = answerContainer.wordsByPosition
        val correctAnswer = words[0].from.split(' ')

        if(answer.size != correctAnswer.size) return Result(false, words[0].from, words[0])

        for(i in answer.indices){
            if(answer[i] != correctAnswer[i]) return Result(false, words[0].from, words[0])
        }

        return Result(true, words[0].from, words[0])
    }

    private fun animateTranslation(word: CustomTextView, to: Pair<Int,Int>){
        val currentCoordinates = IntArray(2)
        val rootCoordinates = IntArray(2)

        binding.root.getLocationOnScreen(rootCoordinates)
        word.getLocationOnScreen(currentCoordinates)

        currentCoordinates[0] -= rootCoordinates[0]
        currentCoordinates[1] -= rootCoordinates[1]

        val animationX = ObjectAnimator.ofFloat(
            word,
            "translationX",
            currentCoordinates[0].toFloat(),
            to.first.toFloat())
        val animationY = ObjectAnimator.ofFloat(
            word,
            "translationY",
            currentCoordinates[1].toFloat(),
            to.second.toFloat()
        )

        AnimatorSet().apply {
            interpolator = LinearInterpolator()
            duration = 100
            playTogether(animationX, animationY)
            start()
        }
    }

    /**
     * Moves [view] from words container to answer container
     */
    private fun fromWordCtoAnswerCHandler(view: CustomTextView){
        wordsContainer.removeWord(view)
        animateTranslation(
            view,
            answerContainer.addWord(view).getValue(view)
        )

        view.setOnClickListener {
            fromAnswerCtoWordCHandler(view)
        }
    }

    /**
     * Moves [view] from answer container back to the words container
     */
    private fun fromAnswerCtoWordCHandler(view: CustomTextView) {
        animateTranslation(
            view,
            wordsContainer.addWord(view).getValue(view)
        )

        for((wordView, coordinates) in answerContainer.removeWord(view)){
            animateTranslation(wordView, coordinates)
        }

        view.setOnClickListener {
            fromWordCtoAnswerCHandler(view)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestSentenceBinding.inflate(inflater, container, false)

        binding.words.background = Tools.getCircleLayout(R.color.gray, requireContext(), resources)

        val margin = 40
        val spaceBetweenWords = 10
        val lineHeight = 40
        val offset = 10

        val splitWords: MutableList<String> = words[0].from.split(' ').toMutableList()
        val wordsContainerCoordinates = Pair(
            Tools.dp2Pixels(margin, resources),
            Tools.dp2Pixels(300, resources)
        )
        val answerContainerCoordinates = Pair(
            Tools.dp2Pixels(margin, resources),
            Tools.dp2Pixels(110-offset, resources)
        )

        wordsContainer = WordsContainer(
            Pair(wordsContainerCoordinates.first, wordsContainerCoordinates.second),
            Tools.getScreenWidth(resources) - Tools.dp2Pixels(2*margin, resources),
            Tools.dp2Pixels(lineHeight, resources),
            Tools.dp2Pixels(spaceBetweenWords, resources),
            false
        )
        answerContainer = WordsContainer(
            Pair(answerContainerCoordinates.first, answerContainerCoordinates.second),
            Tools.getScreenWidth(resources) - Tools.dp2Pixels(2*margin, resources),
            Tools.dp2Pixels(lineHeight+offset, resources),
            Tools.dp2Pixels(spaceBetweenWords, resources),
            true
        )

        val wordsSize = splitWords.size
        for(i in 0 until wordsSize){
            val randomNumber = (0 until splitWords.size).random()
            val view = CustomTextView(requireContext(), splitWords[randomNumber])

            val coordinates = wordsContainer.addWord(view)
            val params = ConstraintLayout.LayoutParams(
                view.customWidth,
                view.customHeight
            )
            view.translationX = coordinates.getValue(view).first.toFloat()
            view.translationY = coordinates.getValue(view).second.toFloat()

            view.setOnClickListener {
                fromWordCtoAnswerCHandler(view)
            }
            view.background = Tools.getCircleLayout(R.color.magenta, requireContext(), resources)

            binding.root.addView(view, params)

            splitWords.removeAt(randomNumber)
        }

        return binding.root
    }
}