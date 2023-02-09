package com.example.vocab.gui

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.vocab.R
import com.example.vocab.Tools

/**
 * Text view with resizeable layout
 * @property customHeight provides height of textView before the view itself is created
 * @property customHeight provides width of textView before the view itself is created
 */
class CustomTextView(context: Context, val text: String) : ConstraintLayout(context) {
    var customWidth = 0
        private set
    var customHeight = 0
        private set

    companion object{
        const val textSize = 7
    }

    init {
        inflate(context, R.layout.fragment_custom_text_view, this)
        this.findViewById<TextView>(R.id.homeworkName)

        val multiplyConst = Tools.spToPx(7.toFloat(), context)

        customWidth = text.length*multiplyConst + Tools.dp2Pixels(25, resources)
        customHeight = Tools.dp2Pixels(32, resources)

        layoutParams = ViewGroup.LayoutParams(
            width,
            height
        )

        findViewById<TextView>(R.id.customTextViewHomework).text = text
    }
}