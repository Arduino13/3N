package com.example.vocab.scanner

import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentWordsSelectBinding
import com.example.vocab.thirdParty.OCRMLKit
import com.example.vocab.thirdParty.Translator
import com.google.mlkit.vision.common.InputImage

/**
 * Fragment with [CustomCanvasView] where user choose words that he want to add
 */
class WordsSelectFragment : Fragment() {
    private lateinit var binding: FragmentWordsSelectBinding
    private lateinit var fragmentModel: ScannerModel
    private lateinit var words: Map<String, Rect>

    //from https://stackoverflow.com/questions/16193282/how-to-get-the-position-of-a-picture-inside-an-imageview/26930852
    private fun getBitmapPositionInsideImageView(imageView: ImageView?): IntArray {
        val ret = IntArray(4)
        if (imageView == null || imageView.drawable == null) return ret

        val f = FloatArray(9)
        imageView.imageMatrix.getValues(f)

        val scaleX = f[Matrix.MSCALE_X]
        val scaleY = f[Matrix.MSCALE_Y]


        val d = imageView.drawable
        val origW = d.intrinsicWidth
        val origH = d.intrinsicHeight

        val actW = Math.round(origW * scaleX)
        val actH = Math.round(origH * scaleY)
        ret[2] = actW
        ret[3] = actH

        val imgViewW = imageView.width
        val imgViewH = imageView.height
        val top = (imgViewH - actH) / 2
        val left = (imgViewW - actW) / 2
        ret[0] = left
        ret[1] = top
        return ret
    }

    /**
     * Decides which step come next
     */
    private fun addWords(){
        //In case of structured text, in the first phase when user is loading source words, and
        //next step is to load their translations
        if(fragmentModel.process == ScannerModel.scanningProcess.scanningFromWords){
            val list = mutableListOf<Word>()

            for((box, selected) in binding.canvasLayer.selectedBoxes){
                if(selected){
                    for((string, boxInter) in words){
                        if(box == boxInter){
                            list.add(Word(id=Tools.getUUID(), from=string, to=""))
                        }
                    }
                }
            }

            fragmentModel.selectedWords = list
            fragmentModel.process = ScannerModel.scanningProcess.scanningToWords

            binding.root.findNavController().navigate(R.id.toStart)
        }
        //in case of structured text, when source words and their translations is loaded
        else if(fragmentModel.process == ScannerModel.scanningProcess.scanningToWords){
            val list = mutableListOf<Word>()
            list.addAll(fragmentModel.selectedWords ?: listOf())

            val selectedBoxes = binding.canvasLayer.selectedBoxes

            var index = 0
            for((box, selected) in selectedBoxes){
                if(selected){
                    for((string, boxInter) in words){
                        if(box == boxInter && index < list.size){
                            list[index] = list[index].copy(to=string)
                            index += 1
                        }
                    }
                }
            }

            fragmentModel.selectedWords = list

            binding.root.findNavController().navigate(R.id.toResults)
        }
        //in case of unstructured text when translations are obtained from google translator
        else if(fragmentModel.process == ScannerModel.scanningProcess.undefined){
            val list = mutableListOf<Word>()

            val selectedBoxes = binding.canvasLayer.selectedBoxes

            var index = 0
            var selectedWords = 0
            for((box, selected) in selectedBoxes){
                if(selected){
                    selectedWords += 1
                    for((string, boxInter) in words){
                        if(box == boxInter){
                            Translator.translate(
                                Translator.fromID((Database(requireContext(), requireActivity()).getSetting(
                                    Settings.language) as? Int) ?: Settings.languageDef.language) ?: Settings.languageDef,
                                Translator.languages.cs,
                                string
                            ){ translation ->
                                list.add(Word(id=Tools.getUUID(), from=string, to=translation))
                                if(index == (selectedWords-1)){
                                    fragmentModel.selectedWords = list
                                    binding.root.findNavController().navigate(R.id.toResults)
                                }
                                else{
                                    index += 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWordsSelectBinding.inflate(inflater, container, false)

        val _fragmentModel: ScannerModel by activityViewModels()
        fragmentModel = _fragmentModel

        (activity as AppCompatActivity).supportActionBar?.title  = resources.getString(R.string.vocabulary_scanner_choose_words)

        if(fragmentModel.imageBitMap == null) throw Exception("internal error - imageBitMap = null")
        if(fragmentModel.mode == null) throw Exception("internal error - mode = null")
        if(fragmentModel.process == null) throw Exception("internal error - process = null")

        binding.picture.setImageBitmap(fragmentModel.imageBitMap!!)

        OCRMLKit().recognizeText(
            InputImage.fromBitmap(fragmentModel.imageBitMap!!, 0),
            fragmentModel.mode == ScannerModel.scanningMode.structuredText
        ){ words ->
            val wordsScaled = mutableMapOf<String, Rect>()

            val ret = getBitmapPositionInsideImageView(binding.picture)

            val widthRatio: Float = fragmentModel.imageBitMap!!.width.toFloat() /
                    (Tools.getScreenWidth(resources) - ret[0]*2).toFloat()
            val heightRatio: Float = fragmentModel.imageBitMap!!.height.toFloat() /
                    (binding.canvasLayer.height - ret[1]*2).toFloat()

            for((text, box) in words){
                wordsScaled[text] =
                    Rect(
                        (box.left/widthRatio).toInt()+ret[0],
                        (box.top/heightRatio).toInt()+ret[1],
                        (box.right/widthRatio).toInt()+ret[0],
                        (box.bottom/heightRatio).toInt()+ret[1]
                    )
            }

            this.words = wordsScaled
            binding.canvasLayer.drawBoxes(wordsScaled.values.toList())
        }

        binding.clear.setOnClickListener {
            binding.canvasLayer.drawBoxes(words.values.toList())
        }

        binding.saveWords.setOnClickListener {
            addWords()
        }

        return binding.root
    }
}