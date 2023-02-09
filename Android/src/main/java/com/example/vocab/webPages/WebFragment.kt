package com.example.vocab.webPages

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentWebListBinding
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration

/**
 * A fragment representing a list of [BaseArticle] objects
 */
class WebFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentWebListBinding.inflate(inflater, container, false)
        val view = binding.listView

        val model: globalModel by activityViewModels()

        val factory = WebListModelFactory(model,
            Database(requireContext(), requireActivity()).getSetting(Settings.rssNum) as Int? ?: 10)
        val fragmentModel = ViewModelProvider(this, factory).get(WebListModel::class.java)

        val adapterT = BaseListAdapter()

        fragmentModel.data.observe(viewLifecycleOwner, Observer{ new ->
            new?.let{
                binding.loadingBar.visibility = View.GONE

                for(item in new){
                    if(item is BaseArticle){
                        item.touchHandler = BaseArticle.TouchListenerClass(){ link ->
                            val bundle = bundleOf("link" to link)
                            view.findNavController().navigate(R.id.openWeb, bundle)
                        }
                    }
                }
                if(new.isEmpty()) binding.warningLabel.visibility = View.VISIBLE
                adapterT.submitList(new)
            } ?: run{binding.loadingBar.visibility = View.VISIBLE}
        })

        binding.navBar.setNavigationOnClickListener {
            binding.root.findNavController().navigate(R.id.webList)
        }

        // Set the adapter
        with(view) {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterT
        }

        binding.warningLabel.visibility = View.GONE
        view.addItemDecoration(LinearSpacingDecoration(itemSpacing = 30, edgeSpacing = 20))
        return binding.root
    }
}