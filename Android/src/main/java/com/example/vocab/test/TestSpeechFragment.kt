package com.example.vocab.test

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.vocab.R
import com.example.vocab.databinding.FragmentTestSpeechBinding
import com.example.vocab.mediaUtils.AudioRecorder
import com.example.vocab.thirdParty.PyTorchNN
import com.example.vocab.thirdParty.Translator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * Tests pronunciation, doesn't work in reverse order, because it makes no sense
 */
class TestSpeechFragment : GenericTest<FragmentTestSpeechBinding>() {
    override val maxWordLength: Int = 3
    override val minWordLength: Int = -1
    override val numberOfRequiredWords: Int = 1

    var counter: Int = 0

    var sentence: String? = null
    val debug: Boolean = true
    var blockRecord: Boolean = false

    //prevzato z: https://gist.github.com/ademar111190/34d3de41308389a0d0d8
    private fun levenshtein(lhs : CharSequence, rhs : CharSequence) : Int {
        val lhsLength = lhs.length + 1
        val rhsLength = rhs.length + 1

        var cost = Array(lhsLength) { it }
        var newCost = Array(lhsLength) { 0 }

        for (i in 1..rhsLength-1) {
            newCost[0] = i

            for (j in 1..lhsLength-1) {
                val match = if(lhs[j - 1] == rhs[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = Math.min(Math.min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[lhsLength - 1]
    }

    override fun checkAndPost(): Result {
        sentence?.let {
            var colapseWordFrom = ""
            var temp: Char = ' '
            for (c in words[0].from) {
                if (c != temp) {
                    colapseWordFrom += c
                    temp = c
                }
            }

            val distance = levenshtein(sentence!!, colapseWordFrom.toLowerCase())
            val netError: Float = words[0].from.length * 10 / 100f

            return if (distance.toFloat() - netError > 2f) {
                Result(false, words[0].from, words[0])
            } else {
                Result(true, words[0].from, words[0])
            }
        }

        return Result(false, words[0].from, words[0])
    }

    /**
     * Starts 3 second recording with evaluation using [PyTorchNN]
     */
    private fun record(){
        if(!blockRecord) {
            binding.runRecording.setBackgroundColor(resources.getColor(R.color.red))
            blockRecord = true

            val record = AudioRecorder(3)
            counter+=1
            record.startRecording(counter) { data ->
                sentence = PyTorchNN(requireContext()).recognizeSpeech(data)
                if (debug) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(
                            requireContext(),
                            sentence,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                blockRecord = false
                binding.runRecording.setBackgroundColor(resources.getColor(R.color.green))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestSpeechBinding.inflate(inflater, container, false)

        binding.runRecording.setOnClickListener {
            record()
        }
        binding.runRecording.setBackgroundColor(resources.getColor(R.color.green))
        binding.wordToTest.text =  words[0].from

        val tts = TextToSpeech(context){}

        binding.playAudio.setOnClickListener {
            tts.language = Locale.US
            tts.speak(words[0].from, TextToSpeech.QUEUE_ADD, null)
        }

        return binding.root
    }
}