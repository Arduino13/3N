package com.example.vocab.thirdParty

import android.content.Context
import android.util.TimingLogger
import com.example.vocab.AssetsUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.File
import java.lang.Exception

/**
 * This class was for experimental purposes, now it's unused and probably not working
 */
class TensorflowNN(private val context: Context) {
    val compatList = CompatibilityList()

    val options = Interpreter.Options().apply{
        if(compatList.isDelegateSupportedOnThisDevice){
            this.addDelegate(GpuDelegate(compatList.bestOptionsForThisDevice))
            println("using_gpu")
        } else {
            this.setNumThreads(2)
            println("using_cpu")
        }
    }

    private fun finalshape(test: FloatArray): Array<Array<FloatArray>> {
        var toReturn: Array<Array<FloatArray>> = arrayOf()

        for(i in 0 until 196){
            var temp2 = arrayOf<FloatArray>()
            for(x in 0 until 161){
                val temp = FloatArray(1)
                temp[0] = test[i+x*196]

                temp2 += temp
            }

            toReturn += temp2
        }

        return toReturn
    }

    private fun flattenArray(array: Array<Array<FloatArray>>): FloatArray{
        val toReturn = FloatArray(196*29)
        for(t in array){
            for((index_X, x) in t.withIndex()){
                for((index_Z, z) in x.withIndex()){
                    toReturn[index_X*29+index_Z] = z
                }
            }
        }

        return toReturn
    }

    val interpreter = Interpreter(File(AssetsUtils.assetFilePath(context, "model_mobile.tflite")), options)

    /**
     * For given record[input] returns recognized text
     */
    fun recognizeSpeech(input: FloatArray): String{
        val melSpectrogram = NNUtils.melSpectrogram(input)
        var output = arrayOf<FloatArray>()
        for(i in 0 until 196){
            output += FloatArray(29)
        }
        val outputFinal = arrayOf(output)

        val input = arrayOf(finalshape(melSpectrogram))
        interpreter.run(input, outputFinal)

        return NNUtils.decodeText(flattenArray(outputFinal))
    }
}