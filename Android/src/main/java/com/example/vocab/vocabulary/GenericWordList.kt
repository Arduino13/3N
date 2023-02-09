package com.example.vocab.vocabulary

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.*
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.basic.Word
import com.example.vocab.globalModel
import com.example.vocab.gui.BaseItem
import com.example.vocab.gui.BaseListAdapter
import com.example.vocab.gui.LinearSpacingDecoration
import com.example.vocab.scanner.ScannerActivity
import com.example.vocab.studentVocabulary.VocabularyModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.lang.Exception

/**
 * Generic class for list of words, which handles their removing, adding and transitions between fragments
 *
 * @property header words's list name
 * @property accessId id for accessing model data given by [GenericVocabularyModel] child
 * @property actionButton button for adding new word
 * @property destNewWord action's id to move to fragment for adding new word
 */
abstract class GenericWordList<T : ViewBinding>(private val destNewWord: Int): Fragment(), GestureDetector.OnGestureListener {
    protected lateinit var binding: T
    protected lateinit var adapterT: BaseListAdapter
    protected lateinit var fragmentModel: GenericVocabularyModel
    protected lateinit var fragmentSettingModel: VocabularySettingModel
    protected var header: String? = null
    protected lateinit var accessId: String
    private lateinit var listView: RecyclerView
    private lateinit var navBar: MaterialToolbar
    private lateinit var actionButton: FloatingActionButton

    private var editable: Boolean = true

    /**
     * Used to remove words from list
     */
    protected open fun setFragmentToEditMode(mode: Boolean, selectedCheckbox: CheckBox? = null){
        navBar.menu.findItem(R.id.remove_vocabulary_list).isVisible = mode
        navBar.menu.findItem(R.id.cancel_vocabulary_list).isVisible = mode

        if(!editable) navBar.findViewById<View>(R.id.take_photo_vocabulary).visibility = View.GONE

        fragmentModel.words.value?.let {
            for(item in adapterT.currentList){
                (item as? BaseWord)?.let{
                    it.editable = mode
                    if(!mode) it.isChecked = false
                    if(it.checkBoxRef == selectedCheckbox) it.isChecked = true
                }
            }
        }
    }

    /**
     * Sets up [BaseWord] list
     */
    protected open fun setUpList(list: List<BaseWord>, header: String?): List<BaseItem<*>>{
        val toReturn = mutableListOf<BaseItem<*>>()
        for(w in list){
            if (w.word.list == header || w.word.newList) {
                if (w.word.class_id != fragmentModel.id.value!!){
                    actionButton.visibility = View.GONE
                    navBar.findViewById<View>(R.id.take_photo_vocabulary).visibility = View.GONE

                    editable = false
                }

                w.layout = Tools.getCircleLayout(R.color.gray, requireContext(), resources)

                toReturn += w
            }
        }

        return toReturn
    }

    /**
     * Deletes selected words
     */
    protected open fun deleteSelected(){
        val toDelete = mutableListOf<BaseWord>()
        var showMessage = false

        for (item in adapterT.currentList) {
            (item as? BaseWord)?.let {
                if (it.isChecked && it.word.class_id == fragmentModel.id.value!!) toDelete += it
                else if(it.isChecked){
                    showMessage = true
                }
            }
        }

        if(showMessage){
            Toast.makeText(
                requireContext(),
                resources.getText(R.string.vocabulary_can_not_delete),
                Toast.LENGTH_LONG).show()
        }

        fragmentModel.delete(toDelete, accessId)
    }

    /**
     * Called when moving back in navigation stack, releases access to model
     */
    protected fun popBack(){
        onPopBack()
        fragmentModel.releaseAccess()
        fragmentSettingModel.accessId = null
        binding.root.findNavController().popBackStack()
    }

    /**
     * In case when [popBack] is not called
     */
    override fun onDetach() {
        if(fragmentSettingModel.accessId != null) {
            fragmentModel.releaseAccess()
            fragmentSettingModel.accessId = null
        }

        super.onDetach()
    }

    /**
     * Event which is called when [actionButton] is pressed
     */
    protected open fun onAddWord() {}

    /**
     * Event which is called in [popBack] before releasing access to model
     */
    protected open fun onPopBack() {}

    /**
     * Initializes fragment
     */
    @SuppressLint("ClickableViewAccessibility")
    protected fun initList(binding: T, listView: RecyclerView,
                           navBar: MaterialToolbar,
                           actionButton: FloatingActionButton,
                           fragmentModel: GenericVocabularyModel){
        this.binding = binding
        this.listView = listView
        this.navBar = navBar
        this.actionButton = actionButton

        adapterT = BaseListAdapter()

        val _fragmentSettingModel: VocabularySettingModel by viewModels()
        fragmentSettingModel = _fragmentSettingModel

        accessId = fragmentModel.requestAccess() ?: fragmentSettingModel.accessId ?: throw Exception("Access to fragment view model denied")
        fragmentSettingModel.accessId = accessId

        this.fragmentModel = fragmentModel

        header = arguments?.getString("header")

        with(listView){
            layoutManager = LinearLayoutManager(context)
            adapter = adapterT
        }

        fragmentModel.words.observe(viewLifecycleOwner, Observer{ new ->
            new?.let {
                adapterT.submitList(setUpList(new, header))
            }
        })

        navBar.setOnMenuItemClickListener { menuItem -> //Sets up navigation bar buttons
            when(menuItem.itemId){
                R.id.remove_vocabulary_list -> {
                    deleteSelected()
                    setFragmentToEditMode(false)
                    true
                }
                R.id.cancel_vocabulary_list -> {
                    setFragmentToEditMode(false)
                    true
                }
                R.id.take_photo_vocabulary ->{ //Runs scanner activity
                    val photoFinished = activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            val baseWordList = mutableListOf<BaseItem<*>>()
                            for((from, to) in result.data?.getBundleExtra("words")?.get("words") as Map<String, String>){
                                baseWordList.add(BaseWord(Word(id=Tools.getUUID(),
                                    class_id=fragmentModel.id.value!!,
                                    from=from,
                                    to=to,
                                    list=header ?: "",
                                    newList=header==null)))
                            }

                            fragmentModel.save(baseWordList, accessId)
                        }
                    }
                    val intent = Intent(activity, ScannerActivity::class.java)
                    photoFinished?.launch(intent)

                    true
                }
                else -> {
                    false
                }
            }
        }

        navBar.setOnClickListener {
            popBack()
        } //overriding default popback action
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                popBack()
            }
        }) //overriding default popback action, but when android return button is pressed

        actionButton.setOnClickListener {
            onAddWord()

            val data = bundleOf("accessId" to accessId, "list" to header)
            binding.root.findNavController().navigate(destNewWord, data)
        }

        val itemSpacing = Tools.dp2Pixels(15, resources)
        listView.addItemDecoration(LinearSpacingDecoration(itemSpacing, itemSpacing))

        val mDetector = GestureDetectorCompat(requireContext(), this)
        mDetector.setIsLongpressEnabled(true)
        listView.setOnTouchListener { _, motionEvent ->
            mDetector.onTouchEvent(motionEvent)
        } //for detecting long press to remove words

        navBar.menu.findItem(R.id.remove_vocabulary_list).isVisible = false
        navBar.menu.findItem(R.id.cancel_vocabulary_list).isVisible = false
    }

    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

    override fun onDown(p0: MotionEvent?): Boolean = false

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?){
        p0?.let {
            val selectedCheckBox = listView.findChildViewUnder(it.x, it.y)?.findViewById<CheckBox>(R.id.checked)
            setFragmentToEditMode(true, selectedCheckBox)
        }
    }
}