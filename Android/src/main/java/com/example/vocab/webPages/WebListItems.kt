package com.example.vocab.webPages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vocab.R
import com.example.vocab.basic.Student
import com.example.vocab.databinding.FragmentWebListItemsBinding
import com.example.vocab.globalModel

/**
 * List of saved web pages RSS
 */
class WebListItems : Fragment(), GestureDetector.OnGestureListener{
    private lateinit var binding: FragmentWebListItemsBinding
    private lateinit var adapterT: WebRecyclerViewList
    private lateinit var model: globalModel

    private fun deleteSelected(){
        val student = model.data.value as Student

        for((ch,toDelete) in adapterT.checkBoxes){
            if(toDelete){
                student.removeWeb(ch)
            }
        }

        model.setData(student)
    }

    /**
     * Shows checkboxes to select pages to remove
     */
    private fun setFragmentToEditMode(mode: Boolean, selectedCheckBox: CheckBox? = null){
        binding.navBar.menu.findItem(R.id.remove_web_list).isVisible = mode
        binding.navBar.menu.findItem(R.id.cancel_web_list).isVisible = mode

        selectedCheckBox?.let {
            adapterT.setCheckbox(selectedCheckBox, true)
        }

        adapterT.editable = mode
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWebListItemsBinding.inflate(inflater, container, false)
        val view = binding.listView
        val _model: globalModel by activityViewModels() //because this function need to run after some procedures
        model = _model

        adapterT = WebRecyclerViewList(model)
        model.data.observe(viewLifecycleOwner, Observer{
            adapterT.loadData()
        })

        // Set the adapter
        with(view) {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterT
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }


        binding.navBar.setNavigationOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        binding.navBar.menu.findItem(R.id.remove_web_list).isVisible = false
        binding.navBar.menu.findItem(R.id.cancel_web_list).isVisible = false

        binding.navBar.setOnMenuItemClickListener { menuItem -> //sets up navigation bar in right upper corner
            when(menuItem.itemId){
                R.id.remove_web_list -> {
                    deleteSelected()
                    setFragmentToEditMode(false)
                    true
                }
                R.id.cancel_web_list -> {
                    setFragmentToEditMode(false)
                    true
                }
                else -> {
                    false
                }
            }
        }

        val mDetector = GestureDetectorCompat(requireContext(), this)
        mDetector.setIsLongpressEnabled(true)
        binding.listView.setOnTouchListener { _, motionEvent ->
            mDetector.onTouchEvent(motionEvent)
        }

        binding.newPage.setOnClickListener {
            binding.root.findNavController().navigate(R.id.addWebItem)
        }

        return binding.root
    }

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

    override fun onDown(p0: MotionEvent?): Boolean = false

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) {
        p0?.let {
            val selectedCheckBox = binding.listView.findChildViewUnder(it.x, it.y)?.findViewById<CheckBox>(R.id.selected)
            setFragmentToEditMode(true, selectedCheckBox)
        }
    }
}