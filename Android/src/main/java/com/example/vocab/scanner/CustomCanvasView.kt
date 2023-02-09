package com.example.vocab.scanner

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import java.util.jar.Attributes

/**
 * Custom canvas with transparent background for selecting scanned words
 *
 * @property selectedBoxes words that were selected
 *
 */
class CustomCanvasView(context: Context, attrs: AttributeSet): View(context, attrs){
    private val touchTolerance = ViewConfiguration.get(context).scaledTouchSlop

    private var listOfBoxes: List<Rect>? = null
    private val changedBoxes = mutableMapOf<Rect, Boolean>()

    val selectedBoxes: Map<Rect, Boolean>
        get(){
            return changedBoxes
        }


    private val drawColor = Color.TRANSPARENT
    private val backgroundColor = 125 and 0xff shl 24 or (0 and 0xff shl 16) or (0 and 0xff shl 8) or (0 and 0xff)

    private val paint = Paint().apply {
        color = drawColor
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        maskFilter = null
        strokeWidth = 50f
    }

    private val paintGRAY = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.BEVEL
        strokeCap = Paint.Cap.SQUARE
        strokeWidth = 7f
    }

    private val paintBLUE = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.BEVEL
        strokeCap = Paint.Cap.SQUARE
        strokeWidth = 7f
    }


    private lateinit var extraBitmap: Bitmap
    private lateinit var extraRectMap: Bitmap

    private lateinit var extraCanvas: Canvas
    private lateinit var extraRectCanvas: Canvas

    private var currentX = 0f
    private var currentY = 0f

    private var motionTouchEventX = 0f
    private var motionTouchEventY = 0f

    private var path = Path()

    /**
     * redraws canvas
     */
    private fun reinit(){
        if(this::extraRectMap.isInitialized) extraRectMap.recycle()
        extraRectMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        extraRectCanvas = Canvas(extraRectMap)

        if (this::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        extraCanvas = Canvas(extraBitmap)
        extraCanvas.drawColor(backgroundColor)
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        reinit()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
        canvas.drawBitmap(extraRectMap, 0f, 0f, null)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        motionTouchEventX = event.x
        motionTouchEventY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> touchStart()
            MotionEvent.ACTION_MOVE -> touchMove()
            MotionEvent.ACTION_UP -> touchUp()
        }
        return true
    }

    /**
     * begins drawing new path
     */
    private fun touchStart() {
        path.reset()
        path.moveTo(motionTouchEventX, motionTouchEventY)
        currentX = motionTouchEventX
        currentY = motionTouchEventY
    }

    /**
     * draws path and change box's borders color to blue when path is intersecting them
     */
    private fun touchMove() {
        val dx = Math.abs(motionTouchEventX - currentX)
        val dy = Math.abs(motionTouchEventY - currentY)
        if (dx >= touchTolerance || dy >= touchTolerance) {
            path.quadTo(currentX, currentY, (motionTouchEventX + currentX) / 2, (motionTouchEventY + currentY) / 2)
            currentX = motionTouchEventX
            currentY = motionTouchEventY

            extraCanvas.drawPath(path, paint)
        }

        listOfBoxes?.let {
            for (box in it){
                if(box.contains(motionTouchEventX.toInt(), motionTouchEventY.toInt())){
                    changedBoxes[box] = true
                    extraRectCanvas.drawRect(box, paintBLUE)
                }
                else if(changedBoxes[box] != true){
                    changedBoxes[box] = false
                }
            }

            invalidate()
        }

        invalidate()
    }

    /**
     * reset path state so it's start doesn't connect to existed one
     */
    private fun touchUp() {
        path.reset()
    }

    /**
     * draw grey [boxes] around detected words
     */
    fun drawBoxes(boxes: List<Rect>){
        changedBoxes.clear()
        reinit()
        listOfBoxes = boxes

        for(box in boxes){
            changedBoxes[box] = false
            extraRectCanvas.drawRect(box, paintGRAY)
        }

        invalidate()
    }
}