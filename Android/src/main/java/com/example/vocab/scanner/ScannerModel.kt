package com.example.vocab.scanner

import android.graphics.Bitmap
import android.widget.Toolbar
import androidx.lifecycle.ViewModel
import com.example.vocab.basic.Word

/**
 * Model for storing data between fragments transitions
 *
 * @property imageBitMap for storing image of text
 * @property rotation determines if image was taken in vertical or in horizontal position
 * @property mode current scanner mode
 * @property process current state of scanner
 * @property selectedWords list of words which were selected in [WordsSelectFragment]
 */
class ScannerModel: ViewModel(){
    enum class scanningMode(var content: String){
        structuredText("structured"),
        unstructuredTxt("unstructured")
    }

    enum class scanningProcess(var content: String){
        scanningFromWords("structured_from_words"),
        scanningToWords("structured_to_words"),
        undefined("unstructured_words")
    }

    var listName: String? = null

    var toolbar: Toolbar? = null
    var imageBitMap: Bitmap? = null
    var rotation: Int? = null
    var mode: scanningMode? = null
    var process: scanningProcess? = null
    var selectedWords: List<Word>? = null
}