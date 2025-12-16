package com.kpixel.kreatecolorpicker.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.kpixel.kreatecolorpicker.model.GradientStop
import java.util.*

class GradientSeekBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val checkerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val selectedThumbPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val stops = ArrayList<GradientStop>().apply {
        add(GradientStop(Color.BLUE, 0f))
        add(GradientStop(Color.MAGENTA, 1f))
    }

    private var selectedIndex = 0

    // লিসেনার
    var onGradientChanged: ((List<GradientStop>) -> Unit)? = null
    var onStopSelected: ((GradientStop) -> Unit)? = null

    init {
        createCheckerPattern()

        thumbPaint.color = Color.WHITE
        thumbPaint.style = Paint.Style.STROKE
        thumbPaint.strokeWidth = 4f

        selectedThumbPaint.color = Color.YELLOW
        selectedThumbPaint.style = Paint.Style.STROKE
        selectedThumbPaint.strokeWidth = 8f
    }

    private fun createCheckerPattern() {
        val size = 20
        val bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        val p = Paint().apply { color = Color.LTGRAY }
        val bg = Paint().apply { color = Color.WHITE }

        c.drawRect(0f, 0f, size * 2f, size * 2f, bg)
        c.drawRect(0f, 0f, size.toFloat(), size.toFloat(), p)
        c.drawRect(size.toFloat(), size.toFloat(), size * 2f, size * 2f, p)
        checkerPaint.shader = BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    }

    fun removeSelectedStop() {
        if (stops.size <= 2) return

        if (selectedIndex != -1 && selectedIndex < stops.size) {
            stops.removeAt(selectedIndex)

            selectedIndex = (selectedIndex - 1).coerceAtLeast(0)

            onStopSelected?.invoke(stops[selectedIndex])
            onGradientChanged?.invoke(stops)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val r = h / 2f

        canvas.drawRoundRect(0f, 0f, w, h, r, r, checkerPaint)

        stops.sortBy { it.position }

        val colors = stops.map { it.color }.toIntArray()
        val positions = stops.map { it.position }.toFloatArray()

        paint.shader = LinearGradient(0f, 0f, w, 0f, colors, positions, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(0f, 0f, w, h, r, r, paint)

        for ((index, stop) in stops.withIndex()) {
            val cx = stop.position * w
            val safeCx = cx.coerceIn(r, w - r)

            paint.shader = null
            paint.color = stop.color
            paint.style = Paint.Style.FILL
            canvas.drawCircle(safeCx, h / 2f, h / 2.5f, paint)

            if (index == selectedIndex) {
                canvas.drawCircle(safeCx, h / 2f, h / 2.5f, selectedThumbPaint)
            } else {
                canvas.drawCircle(safeCx, h / 2f, h / 2.5f, thumbPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val w = width.toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val clickedIndex = getThumbAt(x)
                if (clickedIndex != -1) {
                    selectedIndex = clickedIndex
                    onStopSelected?.invoke(stops[selectedIndex])
                    invalidate()
                } else {
                    val position = (x / w).coerceIn(0f, 1f)
                    val color = calculateColorAt(position)
                    stops.add(GradientStop(color, position))
                    selectedIndex = stops.size - 1
                    onStopSelected?.invoke(stops[selectedIndex])
                    onGradientChanged?.invoke(stops)
                    invalidate()
                }
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (selectedIndex != -1) {
                    val newPos = (x / w).coerceIn(0f, 1f)
                    stops[selectedIndex].position = newPos
                    onGradientChanged?.invoke(stops)
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun getThumbAt(x: Float): Int {
        val w = width.toFloat()
        val threshold = height.toFloat()

        stops.forEachIndexed { index, stop ->
            val cx = stop.position * w
            if (Math.abs(x - cx) < threshold) {
                return index
            }
        }
        return -1
    }


    private fun calculateColorAt(pos: Float): Int {
        return stops.firstOrNull()?.color ?: Color.WHITE
    }

    fun updateSelectedStopColor(color: Int) {
        if (selectedIndex in stops.indices) {
            stops[selectedIndex].color = color
            invalidate()
            onGradientChanged?.invoke(stops)
        }
    }

    fun getSelectedStop(): GradientStop? {
        return if (selectedIndex in stops.indices) stops[selectedIndex] else null
    }
}