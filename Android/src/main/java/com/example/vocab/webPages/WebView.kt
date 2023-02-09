package com.example.vocab.webPages

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Settings
import com.example.vocab.Tools
import com.example.vocab.basic.Student
import com.example.vocab.basic.Word
import com.example.vocab.database.Database
import com.example.vocab.databinding.FragmentWebViewBinding
import com.example.vocab.globalModel
import com.example.vocab.thirdParty.Translator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

/**
 * WebView for display webpage's content
 */
class WebView : Fragment(), GestureDetector.OnGestureListener{
    private val jQueryScript = "var script = document.createElement('script'); " +
            "script.setAttribute('type','text/javascript'); " +
            "script.src = 'https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js'; " +
            "document.getElementsByTagName('head')[0].appendChild(script); "

    private lateinit var binding: FragmentWebViewBinding
    private var javaScriptLoaded = false

    /**
     * JavaScript for wrapping every word to it's own html element so it can be located using
     * elementFromPoint function
     */
    private fun getScript(x: String, y: String): String{
        var script = "var inX = $x * (window.innerWidth / ${binding.web.width});\n"
        script += "var inY = $y * (window.innerHeight / ${binding.web.height}); \n"
        script += """
        var theWindow = $(window)
        var theElement = document.elementFromPoint(inX, inY);
        var sentence = theElement.textContent;
        var tagName = theElement.tagName;
        var words = sentence.split(' ');
        if(words.length > 1 && 
            (tagName == 'P' || tagName == 'A' || tagName == 'H1' || tagName == 'H2' 
            || tagName == 'H3' || tagName == 'H4' || tagName == 'H5' || tagName == 'UL' || tagName == 'LI'
            || tagName == 'B')){
            theElement.innerHTML = '';
            $.each(words, function(i, w){theElement.innerHTML += ("<span>" + w + " </span>")});
            theElement = document.elementFromPoint(inX, inY);
            theElement.textContent.split(' ')[0];
        }
        else if(words.length == 2 && tagName == 'SPAN'){
            theElement.textContent.split(' ')[0];
        }
        """

        return script
    }

    private lateinit var progressBar: ProgressBar
    private lateinit var webView: android.webkit.WebView
    private lateinit var mDetector: GestureDetectorCompat

    inner class customWebClient: WebChromeClient(){
        override fun onProgressChanged(view: android.webkit.WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            progressBar.progress = newProgress
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebViewBinding.inflate(inflater, container, false)
        progressBar = binding.progressBar
        webView = binding.web

        binding.navBar.setNavigationOnClickListener {
            binding.root.findNavController().popBackStack()
        }

        webView.webChromeClient = customWebClient()
        webView.isLongClickable = false
        webView.settings.javaScriptEnabled = true;
        webView.setOnLongClickListener { true }
        webView.loadUrl(arguments?.getString("link") ?: throw Exception("internal error link = NULL"))
        webView.setWebViewClient(object : WebViewClient() {
            fun shouldOverrideUrlLoading(
                view: WebView,
                url: String?
            ): Boolean {
                return false
            }
        })

        mDetector = GestureDetectorCompat(requireContext(), this)
        mDetector.setIsLongpressEnabled(true)
        webView.setOnTouchListener { _ , motionEvent ->
            mDetector.onTouchEvent(motionEvent)
        }

        Timer("jQueryLoad", false).schedule(1000) {
            CoroutineScope(Dispatchers.Main).launch {
                webView.evaluateJavascript(jQueryScript) {}
            }
        }

        return binding.root
    }


    override fun onShowPress(p0: MotionEvent?) {}

    override fun onSingleTapUp(p0: MotionEvent?): Boolean = false

    override fun onDown(p0: MotionEvent?): Boolean = false

    override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean = false

    override fun onLongPress(p0: MotionEvent?) { //long press for selecting the word on touch's coordinates
        p0?.let {
            webView.evaluateJavascript(
                getScript(
                    p0.x.toString(),
                    p0.y.toString()
                )
            ) { toReturn ->
                val toReturn = toReturn.subSequence(1, toReturn.length-1).toString()

                Translator.translate( //automatic translation
                    Translator.fromID((Database(requireContext(), requireActivity()).getSetting(Settings.language) as? Int)
                            ?: Settings.languageDef.language)
                        ?: Settings.languageDef,
                    Translator.fromID(
                        (Database(requireContext(), requireActivity()).getSetting(Settings.AppLanguage) as? Int)
                            ?: Settings.languageDef.language)
                        ?: Settings.languageDef ,
                    toReturn
                ) { translation ->

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(resources.getString(R.string.webView_add_word_dialog_title))
                        .setMessage(resources.getString(R.string.webView_add_word_dialog_question) + " " + toReturn + "-" + translation)
                        .setNegativeButton(resources.getString(R.string.button_base_negative_no)) { _, _ -> }
                        .setPositiveButton(resources.getString(R.string.button_base_positive)) { _, _ ->
                            val model: globalModel by activityViewModels()
                            val student = model.data.value as Student
                            student.addWord(
                                Word(
                                    Tools.getUUID(),
                                    toReturn,
                                    translation,
                                    class_id = student.id
                                )
                            )

                            model.setData(student)
                        }
                        .show()
                }
            }
        }
    }
}