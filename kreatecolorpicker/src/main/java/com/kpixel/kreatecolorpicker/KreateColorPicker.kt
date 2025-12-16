package com.kpixel.kreatecolorpicker

import android.graphics.Color
import com.kpixel.kreatecolorpicker.model.GradientStop
import com.kpixel.kreatecolorpicker.model.GradientType
import com.kpixel.kreatecolorpicker.views.AlphaSliderView
import com.kpixel.kreatecolorpicker.views.GradientSeekBar
import com.kpixel.kreatecolorpicker.views.HueSliderView
import com.kpixel.kreatecolorpicker.views.SaturationValueView

class KreateColorPicker(
    private val saturationView: SaturationValueView,
    private val hueSlider: HueSliderView,
    private val alphaSlider: AlphaSliderView? = null,
    private val gradientSeekBar: GradientSeekBar? = null
) {

    var activeMode = GradientType.SOLID
    private var currentHue = 0f
    private var currentSat = 1f
    private var currentVal = 1f
    private var currentAlpha = 255

    var onColorChanged: ((Int, String) -> Unit)? = null
    var onGradientChanged: ((List<GradientStop>) -> Unit)? = null

    init {
        setupListeners()
        setColor(Color.RED)
    }

    private fun setupListeners() {
        val solidUpdate = {
            val color = getCurrentSolidColor()
            val hsv = floatArrayOf(currentHue, currentSat, currentVal)
            val solidColor = Color.HSVToColor(255, hsv)
            alphaSlider?.setSolidColor(solidColor)

            val hexCode = String.format("%06X", (0xFFFFFF and color))

            if (activeMode == GradientType.SOLID) {
                onColorChanged?.invoke(color, hexCode)
            } else {
                gradientSeekBar?.updateSelectedStopColor(color)
                onColorChanged?.invoke(color, hexCode)
            }
        }

        hueSlider.onHueChanged = { hue ->
            currentHue = hue
            saturationView.setHue(hue)
            solidUpdate()
        }

        saturationView.onColorChanged = { hsv ->
            currentSat = hsv[1]
            currentVal = hsv[2]
            solidUpdate()
        }

        alphaSlider?.onAlphaChanged = { alpha ->
            currentAlpha = alpha
            solidUpdate()
        }

        gradientSeekBar?.onStopSelected = { stop ->
            setColorInternal(stop.color, updateUI = false)
            val hexCode = String.format("%06X", (0xFFFFFF and stop.color))
            onColorChanged?.invoke(stop.color, hexCode)
        }

        gradientSeekBar?.onGradientChanged = { stops ->
            onGradientChanged?.invoke(stops)
        }
    }
    fun setColor(color: Int) {
        setColorInternal(color, updateUI = true)

        // যদি Linear মোডে থাকি, তাহলে গ্রেডিয়েন্ট বারেও আপডেট পাঠাতে হবে
        if (activeMode == GradientType.LINEAR) {
            gradientSeekBar?.updateSelectedStopColor(color)
        }
    }

    fun deleteSelectedStop() {
        if (activeMode == GradientType.LINEAR) {
            gradientSeekBar?.removeSelectedStop()
        }
    }

    private fun setColorInternal(color: Int, updateUI: Boolean) {
        currentAlpha = Color.alpha(color)
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        currentHue = hsv[0]
        currentSat = hsv[1]
        currentVal = hsv[2]

        hueSlider.setHue(currentHue)
        saturationView.setHue(currentHue)
        saturationView.setSaturationAndValue(currentSat, currentVal)
        alphaSlider?.setSolidColor(color)

        if (updateUI && activeMode == GradientType.SOLID) {
            val hexCode = String.format("%06X", (0xFFFFFF and color))
            onColorChanged?.invoke(color, hexCode)
        }
    }

    fun getCurrentSolidColor(): Int {
        val hsv = floatArrayOf(currentHue, currentSat, currentVal)
        return Color.HSVToColor(currentAlpha, hsv)
    }

    fun setMode(mode: GradientType) {
        this.activeMode = mode

        if (mode != GradientType.SOLID) {
            gradientSeekBar?.getSelectedStop()?.let { stop ->
                setColorInternal(stop.color, updateUI = false)
                val hexCode = String.format("%06X", (0xFFFFFF and stop.color))
                onColorChanged?.invoke(stop.color, hexCode)
            }
        } else {
            val color = getCurrentSolidColor()
            setColor(color)
        }
    }
}