package com.kpixel.kreatecolorpicker.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SaturationValueView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cursorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
    }
    private val cursorShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        color = Color.BLACK
    }

    private var currentHue = 0f
    private var currentSat = 1f
    private var currentVal = 1f

    var onColorChanged: ((FloatArray) -> Unit)? = null
    private val hsv = FloatArray(3)

    // Shaders
    private var valShader: Shader? = null
    private var satShader: Shader? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            // Value Shader (Vertical) - Hue
            valShader = LinearGradient(
                0f, 0f, 0f, h.toFloat(),
                Color.TRANSPARENT, Color.BLACK,
                Shader.TileMode.CLAMP
            )
            updateSatShader()
        }
    }

    private fun updateSatShader() {
        if (width > 0 && height > 0) {
            val hueColor = Color.HSVToColor(floatArrayOf(currentHue, 1f, 1f))
            satShader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                Color.WHITE, hueColor,
                Shader.TileMode.CLAMP
            )
            invalidate()
        }
    }

    fun setHue(hue: Float) {
        currentHue = hue
        updateSatShader()
    }

    fun setSaturationAndValue(sat: Float, `val`: Float) {
        currentSat = sat
        currentVal = `val`
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (satShader == null || valShader == null) return

        // 1. Saturation Layer
        paint.shader = satShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Value Layer
        paint.shader = valShader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 3. Cursor
        val cx = currentSat * width
        val cy = (1f - currentVal) * height

        canvas.drawCircle(cx, cy, 18f, cursorShadowPaint)
        canvas.drawCircle(cx, cy, 18f, cursorPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            currentSat = (event.x / width).coerceIn(0f, 1f)
            currentVal = 1f - (event.y / height).coerceIn(0f, 1f)

            hsv[0] = currentHue
            hsv[1] = currentSat
            hsv[2] = currentVal

            onColorChanged?.invoke(hsv)
            invalidate()
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        }
        return super.onTouchEvent(event)
    }
}