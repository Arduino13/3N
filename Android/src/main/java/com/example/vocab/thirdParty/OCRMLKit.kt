package com.example.vocab.thirdParty

import android.graphics.Point
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OCRMLKit {
    /**
     * Recognizes text in given picture. If [lines] is true than it recognizes lines of words, otherwise
     * it returns each word
     *
     * @return dictionary of keys as word/sentence to their rectangle which bounds it
     */
    fun recognizeText(image: InputImage, lines: Boolean = false, onResult: (Map<String, Rect>)->Unit){
            val recognizer = TextRecognition.getClient()

            recognizer.process(image).addOnSuccessListener { result ->
                val toReturn = mutableMapOf<String, Rect>()

                for (block in result.textBlocks) {
                    for (line in block.lines) {
                        val lineText = line.text
                        val lineFrame = line.boundingBox

                        if (lines) {
                            toReturn[lineText] = lineFrame!!
                        } else {
                            for (word in line.elements) {
                                val wordText = word.text
                                val wordFrame = word.boundingBox

                                toReturn[wordText] = wordFrame!!
                            }
                        }
                    }
                }

                onResult(toReturn)
            }
            .addOnFailureListener { e ->
                    onResult(mutableMapOf<String, Rect>())
            }
    }
}