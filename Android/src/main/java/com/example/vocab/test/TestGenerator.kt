package com.example.vocab.test

import com.example.vocab.Tools
import com.example.vocab.basic.OpenTest
import com.example.vocab.basic.Test
import com.example.vocab.basic.Word

class TestGenerator {
    companion object{
        /**
         * Generates words to be practiced
         */
        fun newTest(wordsInput: List<Word>, maxNumWords: Int, disabledLists: List<String>): OpenTest? {
            if(wordsInput.isEmpty()) return null

            val words = mutableListOf<Word>()
            for(word in wordsInput){
                if(!disabledLists.contains(word.list)) words.add(word)
            }

            val sortedByDate = words.sortedByDescending {
                it.stats.lastTested
            }.toMutableList()

            val sortedByFailRation = words.sortedByDescending {
                if(it.stats.numSucces != 0) {
                    it.stats.numFail / it.stats.numSucces
                }
                else{
                    0
                }
            }.toMutableList()

            val toReturn = mutableListOf<Word>()
            for(i in 0 until maxNumWords){
                if(sortedByDate.isNotEmpty() && sortedByFailRation.isNotEmpty()) {
                    if (i % 2 == 0) {
                        toReturn += sortedByDate.last()
                        sortedByFailRation.remove(sortedByDate.last())
                        sortedByDate.remove(sortedByDate.last())
                    } else {
                        toReturn += sortedByFailRation.last()
                        sortedByDate.remove(sortedByFailRation.last())
                        sortedByFailRation.remove(sortedByFailRation.last())
                    }
                }
            }

            return OpenTest(Tools.getUUID(), toReturn, words.first().class_id)
        }
    }
}