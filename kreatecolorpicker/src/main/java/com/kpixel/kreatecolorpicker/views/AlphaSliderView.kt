package com.kpixel.kreatecolorpicker.views


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class AlphaSliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val checkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val cursorBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val cursorFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var solidColor = Color.RED
    private var currentAlpha = 255
    var onAlphaChanged: ((Int) -> Unit)? = null

    init {
        createCheckerPattern()
    }

    private fun createCheckerPattern() {
        val size = 25
        val bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        val p = Paint().apply { color = Color.LTGRAY }
        val bg = Paint().apply { color = Color.WHITE }

        c.drawRect(0f, 0f, size * 2f, size * 2f, bg)
        c.drawRect(0f, 0f, size.toFloat(), size.toFloat(), p)
        c.drawRect(size.toFloat(), size.toFloat(), size * 2f, size * 2f, p)

        checkerPaint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    fun setSolidColor(color: Int) {
        val newSolid = (color or -0x1000000)
        if (solidColor != newSolid) {
            solidColor = newSolid
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val r = height / 2f

        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), r, r, checkerPaint)


        paint.reset()
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.alpha = 255

        paint.shader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            Color.TRANSPARENT, solidColor,
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), r, r, paint)


        val cx = (currentAlpha / 255f) * width
        val safeCx = cx.coerceIn(r, width - r)
        val cursorRadius = height / 2.2f
        val cursorColor = (solidColor and 0x00FFFFFF) or (currentAlpha shl 24)

        cursorFillPaint.color = cursorColor

        canvas.drawCircle(safeCx, r, cursorRadius, cursorFillPaint)

        canvas.drawCircle(safeCx, r, cursorRadius, cursorBorderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            currentAlpha = ((event.x / width) * 255).toInt().coerceIn(0, 255)
            onAlphaChanged?.invoke(currentAlpha)
            invalidate()
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        }
        return super.onTouchEvent(event)
    }
}