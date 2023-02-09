package com.example.vocab.thirdParty

import android.content.Context
import com.example.vocab.AssetsUtils.Companion.assetFilePath
import com.jlibrosa.audio.JLibrosa
import org.pytorch.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PyTorchNN(context: Context) {
    val model = Module.load(assetFilePath(context, "mobile_model.pt"))

    /**
     * For given record[input] returns recognized text
     */
    fun recognizeSpeech(input: FloatArray): String{
        val data = NNUtils.melSpectrogram(input)
        PyTorchAndroid.setNumThreads(2)

        val buffer = Tensor.allocateFloatBuffer(data.size)
        buffer.put(data)

        val size = LongArray(4)
        size[0] = 1
        size[1] = 1
        size[2] = 161
        size[3] = (data.size/161).toLong()

        val test = System.currentTimeMillis()

        val tensor = Tensor.fromBlob(buffer, size)
        val output = model.forward(IValue.from(tensor)).toTensor().dataAsFloatArray

        println(System.currentTimeMillis() - test)

        return NNUtils.decodeText(output)
    }
}