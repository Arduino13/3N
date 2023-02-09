package com.example.vocab.vocabulary

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.Tools.Companion.getScreenSizeOfDevice
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentVocabularyBinding
import com.example.vocab.gui.*

/**
 * Fragment which displays lists of words, it's inherited by [StudentVocabularyFragment] and
 * [TeacherVocabularyFragment]
 */
abstract class VocabularyFragment(private val destList: Int, private val destNewList: Int): Fragment(), GestureDetector.OnGestureListener{
    private lateinit var binding: FragmentVocabularyBinding
    private lateinit var fragmentModel: GenericVocabularyModel
    private var screenSize = 0.0

    /**
     * Returns layout with round corners
     */
    private fun getShape(w: BaseTextCell): Drawable?{
        val db = Database(requireContext(), requireActivity())

        val shape = ContextCompat.getDrawable(requireContext(), R.drawable.layout_circle)
        val shapeColor = (db.getSetting(w.text) as? Int) ?: run {
            val generatedColor = Colors.generateColor(resources)!!
            db.saveSetting(
                mapOf<String,Int>(
                    w.text to generatedColor
                )
            )
            generatedColor
        }
        shape?.colorFilter = PorterDuffColorFilter(shapeColor, PorterDuff.Mode.OVERLAY)

        return shape
    }

    /**
     * Sets up list based on size of the screen
     */
    private fun setUpList(list: List<BaseTextCell>): List<BaseItem<*>>{
        val toReturn = mutableListOf<BaseItem<*>>()
        val height = if (screenSize>6) Tools.dp2Pixels(80, resources) else Tools.dp2Pixels(40, resources)

        toReturn += BaseHeader(
            resources.getString(R.string.vocabulary_title),
            height + 30
        )

        if (screenSize > 6) {
            for (w in list) {
                w.height = height
                w.alignment = BaseTextCell.wordAlign.center
                w.layoutShape = getShape(w)
            }
        }
        else{
            for(w in list){
                w.height = height
                w.alignment = BaseTextCell.wordAlign.left
                w.layoutShape = getShape(w)
            }
        }

        toReturn.addAll(list)

        val shape = ContextCompat.getDrawable(requireContext(), R.drawable.layout_circle)
        shape?.colorFilter = PorterDuffColorFilter(ResourcesCompat.getColor(resources,R.color.blue, null), PorterDuff.Mode.OVERLAY)
        toReturn += BaseButton(
            resources.getString(R.string.vocabulary_button_title),
            height,
            shape
        ) {
            binding.root.findNavController().navigate(destNewList)
        }

        return toReturn
    }

    /**
     * Returns used model by child objects
     */
    abstract fun getFragmentModelObj(): GenericVocabularyModel

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVocabularyBinding.inflate(inflater, container, false)
        val view = binding.listView

        screenSize = getScreenSizeOfDevice(resources)

        val adapterT = BaseListAdapter()

        ResponsibleList.makeResponsible(view, resources, requireContext())
        view.adapter = adapterT

        fragmentModel = getFragmentModelObj()

        fragmentModel.lists.observe(viewLifecycleOwner, Observer { new ->
            new?.let {
                adapterT.submitList(setUpList(new))
            }
        })

        val mDetector = GestureDetectorCompat(requireContext(), this)
        mDetector.setIsLongpressEnabled(true)
        binding.listView.setOnTouchListener { _, motionEvent ->
            mDetector.onTouchEvent(motionEvent)
        }

        return binding.root
    }

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean {
        p0?.let {
            val headerText = binding.listView.findChildViewUnder(it.x, it.y)?.findViewById<TextView>(R.id.textView)?.text
            headerText?.let {
                val bundle = bundleOf("header" to headerText)
                binding.root.findNavController().navigate(destList, bundle)
                return true
            }
        }

        return false
    }

    override fun onDown(p0: MotionEvent?): Boolean = false

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) {
        println("long")
    }
}