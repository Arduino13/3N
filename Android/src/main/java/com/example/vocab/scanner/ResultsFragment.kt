package com.example.vocab.scanner

import android.app.Instrumentation
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.databinding.FragmentResultsBinding
import com.example.vocab.gui.BaseButton
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration
import com.example.vocab.vocabulary.BaseWord
import kotlinx.android.synthetic.main.activity_scanner.view.*

/**
 * Fragment which summarizes scanned results
 */
class ResultsFragment : Fragment() {
    private lateinit var binding: FragmentResultsBinding
    private lateinit var fragmentModel: ScannerModel

    /**
     * Returns saved words as activity result
     */
    private fun addWords(){
        val words = mutableMapOf<String, String>()

        val data = Intent()
        for(word in fragmentModel.selectedWords ?: listOf()){
            words[word.from] = word.to
        }

        data.putExtra("words", bundleOf("words" to words))

        activity?.setResult(AppCompatActivity.RESULT_OK, data)
        activity?.finish()
    }

    /**
     * Sets list with scanned words and button to save
     */
    private fun setUpList(): List<BaseItem<*>>{
        val toReturn = mutableListOf<BaseItem<*>>()

        (activity as AppCompatActivity).supportActionBar?.title  = resources.getString(R.string.vocabulary_scanner_results)

        for(word in fragmentModel.selectedWords ?: listOf()){
            val baseWord = BaseWord(word)
            baseWord.layout = Tools.getCircleLayout(R.color.gray, requireContext(), resources)

            toReturn.add(baseWord)
        }

        val shape = ContextCompat.getDrawable(requireContext(), R.drawable.layout_circle)
        shape?.colorFilter = PorterDuffColorFilter(ResourcesCompat.getColor(resources,R.color.blue, null), PorterDuff.Mode.OVERLAY)
        val button = BaseButton(
            resources.getString(R.string.button_base_save2),
            Tools.dp2Pixels(40, resources),
            shape
        ) {
            addWords()
        }
        button.textSize = 15f
        toReturn.add(button)

        return toReturn
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentResultsBinding.inflate(inflater, container, false)

        val _fragmentModel: ScannerModel by activityViewModels()
        fragmentModel = _fragmentModel

        val adapterT = BaseListAdapter()

        with(binding.recyclerView){
            adapter = adapterT
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(LinearSpacingDecoration(15, 20))
        }

        adapterT.submitList(setUpList())

        return binding.root
    }
}