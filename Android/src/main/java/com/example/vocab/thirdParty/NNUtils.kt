package com.example.vocab.thirdParty

import com.jlibrosa.audio.JLibrosa

/**
 * Set of utils for evaluation of neural network
 */
class NNUtils {
    companion object{
        private val charMap = mapOf(
            0 to "'",
            1 to " ",
            2 to "a",
            3 to "b",
            4 to "c",
            5 to "d",
            6 to "e",
            7 to "f",
            8 to "g",
            9 to "h",
            10 to "i",
            11 to "j",
            12 to "k",
            13 to "l",
            14 to "m",
            15 to "n",
            16 to "o",
            17 to "p",
            18 to "q",
            19 to "r",
            20 to "s",
            21 to "t",
            22 to "u",
            23 to "v",
            24 to "w",
            25 to "x",
            26 to "y",
            27 to "z"
        )

        /**
         * Converts array of integers to corresponding characters in [charMap]
         */
        private fun intToText(input: Array<Int>): String{
            var outputString = ""
            for(label in input){
                if(label != 28) {
                    outputString += charMap[label]
                }
            }

            return outputString
        }

        /**
         * Transforms 1D array to 2D
         */
        private fun expandArray(input: FloatArray): Array<FloatArray>{
            var output = arrayOf<FloatArray>()

            for(i in input.indices step 29){
                output += FloatArray(29)
                for(x in 0..28){
                    output[i/29][x] = input[i+x]
                }
            }

            return output
        }

        /**
         * Basic argmax function see numpy.argmax
         */
        private fun argMaxes(input: Array<FloatArray>): Array<Int>{
            var output = arrayOf<Int>()

            for(f in input){
                var max: Float? = null
                var index: Int? = null

                for((i, number) in f.withIndex()){
                    if(max == null || number>max){
                        max = number
                        index = i
                    }
                }

                output += index!!
            }

            return output
        }

        /**
         * Decodes output of NN
         */
        fun decodeText(input: FloatArray): String{
            val array = expandArray(input)
            val max = argMaxes(array)
            var string = arrayOf<Int>()

            for((i,m) in max.withIndex()){
                if(i != 0 && max[i-1] != m){
                    string += m
                }
                else if(i == 0) string +=m
            }

            return intToText(string)
        }

        /**
         * basic flatten function see numpy flatten
         */
        private fun flatten(test: Array<FloatArray>): FloatArray{
            val final = FloatArray(test[0].size * test.size)
            var k = 0
            for(elements in test){
                for(element in elements){
                    final[k] = element
                    k += 1
                }
            }

            return final
        }

        /**
         * Returns mel spectrogram of given [data]
         */
        fun melSpectrogram(data: FloatArray): FloatArray{
            return flatten(JLibrosa().generateMelSpectroGram(data, 16000, 512, 161, 256))
        }
    }
}