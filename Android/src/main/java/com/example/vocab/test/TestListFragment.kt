package com.example.vocab.test

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentTestListBinding
import com.example.vocab.globalModel
import com.example.vocab.gui.*
import com.example.vocab.studentVocabulary.VocabularyModel
import com.example.vocab.vocabulary.VocabularyModelFactory

/**
 * Fragment with word's list which can be selected from when generating test
 */
class TestListFragment : Fragment() {
    private lateinit var binding: FragmentTestListBinding

    private fun getShape(w: BaseList): Drawable?{
        val db = Database(requireContext(), requireActivity())

        val shape = ContextCompat.getDrawable(requireContext(), R.drawable.layout_circle)
        val shapeColor = (db.getSetting(w.list.list) as? Int) ?: run {
            val generatedColor = Colors.generateColor(resources)!!
            db.saveSetting(
                mapOf<String,Int>(
                    w.list.list to generatedColor
                )
            )
            generatedColor
        }
        shape?.colorFilter = PorterDuffColorFilter(shapeColor, PorterDuff.Mode.OVERLAY)

        return shape
    }

    private fun setUpList(): List<BaseItem<*>>{
        val model: globalModel by activityViewModels()
        val factory = VocabularyModelFactory(model)
        val vocabularyFragment = ViewModelProvider(requireActivity(), factory).get(VocabularyModel::class.java)

        val disabledListsString = (Database(requireContext(), requireActivity()).getSetting(Settings.disabledWordLists) as? String) ?: ""
        val disabledLists = disabledListsString.split(',')

        val toReturn = mutableListOf<BaseItem<*>>()

        for(list in vocabularyFragment.lists.value ?: listOf()){
            val checkbox = BaseList(Word(id= Tools.getUUID(), from="", to="", list=list.text))
            checkbox.postChecked(!disabledLists.contains(list.text))
            checkbox.shape = getShape(checkbox)

            toReturn.add(checkbox)
        }

        return toReturn
    }

    private fun save(list: List<BaseItem<*>>){
        var toSaveString = ""
        for(chck in list){
            (chck as? BaseList)?.let{
                if(!it.isChecked){
                    toSaveString += "${chck.list.list},"
                }
            }
        }

        if(toSaveString.isNotEmpty()) {
            toSaveString = toSaveString.substring(0, toSaveString.length - 1)
        }

        Database(requireContext(), requireActivity()).saveSetting(
            mapOf(
                Settings.disabledWordLists to toSaveString
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTestListBinding.inflate(inflater, container, false)

        val adapterT = BaseListAdapter()
        with(binding.wordList){
            adapter = adapterT
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(LinearSpacingDecoration(20,15))
        }

        binding.navBar.setOnClickListener {
            save(adapterT.currentList)
            binding.root.findNavController().popBackStack()
        }

        adapterT.submitList(setUpList())

        return binding.root
    }
}