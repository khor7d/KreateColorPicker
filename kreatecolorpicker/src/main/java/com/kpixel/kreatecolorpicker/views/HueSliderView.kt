package com.kpixel.kreatecolorpicker.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class HueSliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
    }
    private val cursorFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var currentHue = 0f
    var onHueChanged: ((Float) -> Unit)? = null

    private val hueColors = intArrayOf(
        Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            paint.shader = LinearGradient(
                0f, 0f, w.toFloat(), 0f,
                hueColors, null, Shader.TileMode.CLAMP
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        val r = height / 2f
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), r, r, paint)

        val cx = (currentHue / 360f) * width
        val safeCx = cx.coerceIn(r, width - r)
        val cursorRadius = height / 2.2f

        // কার্সর ফিল
        cursorFillPaint.color = Color.HSVToColor(floatArrayOf(currentHue, 1f, 1f))
        canvas.drawCircle(safeCx, r, cursorRadius, cursorFillPaint)

        // কার্সর বর্ডার
        canvas.drawCircle(safeCx, r, cursorRadius, cursorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            currentHue = ((event.x / width) * 360f).coerceIn(0f, 360f)
            onHueChanged?.invoke(currentHue)
            invalidate()
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        }
        return super.onTouchEvent(event)
    }

    fun setHue(hue: Float) {
        currentHue = hue
        invalidate()
    }
}