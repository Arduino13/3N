package com.example.vocab.webPages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Student
import com.example.vocab.basic.Web
import com.example.vocab.databinding.FragmentWebAddItemBinding
import com.example.vocab.filters
import com.example.vocab.globalModel

/**
 * Class for addin new web
 */
class WebAddItem : Fragment() {
    private lateinit var binding: FragmentWebAddItemBinding

    /**
     * Validates and saves new data
     */
    private fun checkAndPostData(name: String, _address: String){
        var valid: Boolean = true
        var address = _address

        if(!filters.isValid(name)){
            valid = false
            binding.name.error = resources.getString(R.string.wrong_name_error)
        }

        if(!filters.containsHttp(address)){
            address = "http://$address" //WARNING: can be used to attack via MITM
        }

        if(filters.validAddress(address)){
            if(valid){
                if(RSS(1).testIsRSS(address)){
                    val model: globalModel by activityViewModels()
                    val student = model.data.value as Student

                    student.addWeb(Web(Tools.getUUID(),address,name,student.id))
                    model.setData(student)

                    binding.root.findNavController().popBackStack()
                }
                else{
                    binding.address.error = resources.getString(R.string.webView_non_rss_address_error)
                }
            }
        }
        else{
            valid = false
            binding.address.error = resources.getString(R.string.webView_wrong_address_error)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebAddItemBinding.inflate(inflater, container, false)

        binding.navBar.setNavigationOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        binding.addAction.setOnClickListener {
            checkAndPostData(binding.name.text.toString(), binding.address.text.toString())
        }

        return binding.root
    }
}