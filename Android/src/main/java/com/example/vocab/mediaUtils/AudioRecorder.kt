package com.example.vocab.mediaUtils

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioRecord.READ_BLOCKING
import android.media.MediaRecorder
import android.os.Environment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Class for audio recording
 *
 * @constructor sets [length] property, that is how long the recording should be in seconds, but it's
 * only an approximation
 */

    //TODO: find out why is function called two times
class AudioRecorder(length: Int){
    private var recorder: AudioRecord? = null
    private val bufferSize = AudioRecord.getMinBufferSize(
        16000,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT) * 13*length

    private val output = ShortArray(bufferSize)

    /**
     * Records sound and stores samples in FloatArray that is passed via [onDone] function
     * [counter] is for debugging purposes
     */
    fun startRecording(counter: Int, onDone: (FloatArray)->Unit) {
        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC, 16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        recorder?.startRecording()

        val path = Environment.getExternalStorageDirectory().getPath() + "/" + "test" + counter.toString()

        GlobalScope.launch {
            println(recorder?.sampleRate)
            while (recorder?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val result: Int? = recorder?.read(output, 0, bufferSize, READ_BLOCKING)
                result?.let{
                    val finalOutput = FloatArray(bufferSize)
                    for((i, f) in output.withIndex()){
                        finalOutput[i] = f/32768f
                    }

                    val byteBuffer = ByteBuffer.allocate(finalOutput.size*4)
                    byteBuffer.order(ByteOrder.nativeOrder())
                    for(f in finalOutput) byteBuffer.putFloat(f)

                    /*try{
                        val stream = FileOutputStream(path)
                        stream.write(byteBuffer.array())
                        stream.close()
                    } catch(e: Exception){
                        println("failed")
                    }*/

                    recorder?.stop()
                    recorder?.release()

                    onDone(finalOutput)
                } ?: run{
                    recorder?.stop()
                    recorder?.release()
                }
            }
        }
    }
}