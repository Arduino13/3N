package com.example.vocab.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentRankListBinding
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

/**
 * Fragment which displays student rank list
 */

class RankListFragment : Fragment() {
    private lateinit var binding: FragmentRankListBinding

    private fun setUpList(adapter: BaseListAdapter){
        val model: globalModel by activityViewModels()

        model.getRankList{ result ->
            result?.let{
                val toReturn = mutableListOf<BaseItem<*>>()
                var index = 0
                for((name, values) in result){
                    val cell = BaseRank(index+1, name, values)

                    when(index){
                        0 -> cell.layout = Tools.getCircleLayout(R.color.yellow, requireContext(), resources)
                        1 -> cell.layout = Tools.getCircleLayout(R.color.darkGray, requireContext(), resources)
                        2 -> cell.layout = Tools.getCircleLayout(R.color.brown, requireContext(), resources)
                        else -> cell.layout = Tools.getCircleLayout(R.color.gray, requireContext(), resources)
                    }
                    index += 1

                    toReturn.add(cell)
                }

                CoroutineScope(Dispatchers.Main).async {
                    binding.progressBar.visibility = ViewGroup.GONE
                    adapter.submitList(toReturn)
                }
            } ?: run{
                CoroutineScope(Dispatchers.Main).async{
                    binding.progressBar.visibility = ViewGroup.GONE
                    binding.errorLoadingRankList.visibility = ViewGroup.VISIBLE
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRankListBinding.inflate(inflater, container, false)

        val adapterT = BaseListAdapter()
        with(binding.rankList){
            adapter = adapterT
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(LinearSpacingDecoration(15,20))
        }

        binding.navBar.setOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        binding.errorLoadingRankList.visibility = ViewGroup.GONE
        setUpList(adapterT)

        return binding.root
    }
}