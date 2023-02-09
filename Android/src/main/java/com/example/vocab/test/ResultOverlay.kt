package com.example.vocab.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.vocab.DateUtils
import com.example.vocab.R
import com.example.vocab.databinding.FragmentResultOverlayBinding
import com.example.vocab.generated.callback.OnClickListener

/**
 * Dialog which informs if student's answer was right or wrong
 */
class ResultOverlay : Fragment() {
    companion object{
        /**
         * Displays dialog, [callback] is to notice about closing of dialog
         */
        fun newInstance(result: GenericTest.Result, callback: (ResultOverlay)->Unit): ResultOverlay{
            val newObj = ResultOverlay()
            newObj.setArguments(result, newObj.OnClickListener(callback))

            return newObj
        }
    }

    inner class OnClickListener(private val func: (ResultOverlay)->Unit){
        fun onClick(){
            func(this@ResultOverlay)
        }
    }

    private lateinit var binding: FragmentResultOverlayBinding

    private var result: GenericTest.Result? = null
    private var onClickListener: OnClickListener? = null

    private fun setArguments(result: GenericTest.Result, onClickListener: OnClickListener){
        this.result = result
        this.onClickListener = onClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultOverlayBinding.inflate(inflater, container, false)

        if(result == null) throw Exception("Internal error result == null")

        result?.let{
            if(it.result){
                binding.title.text = resources.getString(R.string.test_result_overlay_yes)
                binding.answer.text = ""
                binding.image.setImageResource(R.drawable.pngegg)
            }
            else{
                binding.title.text = resources.getString(R.string.test_result_overlay_no)
                binding.answer.text = String.format(
                    resources.getString(R.string.test_result_correct_answer),
                    it.correctAnswer
                )
                binding.image.setImageResource(R.drawable.error3)
            }
        }

        binding.onClickListener = onClickListener

        return binding.root
    }
}