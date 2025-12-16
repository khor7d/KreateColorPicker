package com.kpixel.kreatecolortest

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kpixel.kreatecolorpicker.KreateColorPicker
import com.kpixel.kreatecolorpicker.model.GradientStop
import com.kpixel.kreatecolorpicker.model.GradientType
import com.kpixel.kreatecolorpicker.views.AlphaSliderView
import com.kpixel.kreatecolorpicker.views.GradientSeekBar
import com.kpixel.kreatecolorpicker.views.HueSliderView
import com.kpixel.kreatecolorpicker.views.SaturationValueView

class MainActivity : AppCompatActivity() {

    private lateinit var colorPicker: KreateColorPicker

    // Views
    private lateinit var viewPreview: View
    private lateinit var etHexCode: EditText
    private lateinit var gradientContainer: View
    private lateinit var gradientSeekBar: GradientSeekBar

    // Tabs
    private lateinit var tabSolid: TextView
    private lateinit var tabLinear: TextView
    private lateinit var tabRadial: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val satView = findViewById<SaturationValueView>(R.id.saturationView)
        val hueSlider = findViewById<HueSliderView>(R.id.hueSlider)
        val alphaSlider = findViewById<AlphaSliderView>(R.id.alphaSlider)
        val btnDeleteStop = findViewById<ImageButton>(R.id.btnDeleteStop)

        gradientSeekBar = findViewById(R.id.gradientSeekBar)
        viewPreview = findViewById(R.id.viewPreview)
        etHexCode = findViewById(R.id.etHexCode)
        gradientContainer = findViewById(R.id.gradientContainer)

        tabSolid = findViewById(R.id.tabSolid)
        tabLinear = findViewById(R.id.tabLinear)
        tabRadial = findViewById(R.id.tabRadial)


        colorPicker = KreateColorPicker(satView, hueSlider, alphaSlider, gradientSeekBar)


        setupColorListeners()
        setupInputListeners()
        setupTabs()
        setupTouchFocusHandling(satView, hueSlider, alphaSlider, gradientSeekBar)

        btnDeleteStop.setOnClickListener {
            colorPicker.deleteSelectedStop()
        }

        satView.post {
            val startColor = Color.parseColor("#FFFFFF")
            colorPicker.setColor(startColor)
            etHexCode.clearFocus()

            updateTabUI(GradientType.SOLID)
        }
    }

    private fun setupColorListeners() {

        colorPicker.onColorChanged = { color, hex ->

            if (colorPicker.activeMode == GradientType.SOLID) {
                viewPreview.background = null
                viewPreview.setBackgroundColor(color)
            }

            if (!etHexCode.hasFocus()) {
                etHexCode.setText(hex)
            }
        }

        colorPicker.onGradientChanged = { stops ->
            if (colorPicker.activeMode != GradientType.SOLID) {
                updateGradientPreview(stops, colorPicker.activeMode)
            }
        }
    }

    private fun setupInputListeners() {
        etHexCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (etHexCode.hasFocus()) {
                    val input = s.toString().trim()
                    if (input.length == 6 || input.length == 8) {
                        try {
                            val color = Color.parseColor("#$input")
                            colorPicker.setColor(color)
                        } catch (e: Exception) { }
                    }
                }
            }
        })
    }

    private fun setupTabs() {
        // Solid Tab
        tabSolid.setOnClickListener {
            if (colorPicker.activeMode != GradientType.SOLID) {
                colorPicker.setMode(GradientType.SOLID)
                updateTabUI(GradientType.SOLID)

                viewPreview.background = null
                viewPreview.setBackgroundColor(colorPicker.getCurrentSolidColor())
            }
        }

        tabLinear.setOnClickListener {
            if (colorPicker.activeMode != GradientType.LINEAR) {
                colorPicker.setMode(GradientType.LINEAR)
                updateTabUI(GradientType.LINEAR)
            }
        }

        // Radial Tab
        tabRadial.setOnClickListener {
            if (colorPicker.activeMode != GradientType.RADIAL) {
                colorPicker.setMode(GradientType.RADIAL)
                updateTabUI(GradientType.RADIAL)
            }
        }
    }

    // UI (Tab Color & Visibility)
    private fun updateTabUI(mode: GradientType) {
        val activeColor = Color.WHITE
        val inactiveColor = Color.parseColor("#888888")

        tabSolid.setTextColor(inactiveColor)
        tabLinear.setTextColor(inactiveColor)
        tabRadial.setTextColor(inactiveColor)

        when (mode) {
            GradientType.SOLID -> {
                gradientContainer.visibility = View.GONE
                tabSolid.setTextColor(activeColor)
            }
            GradientType.LINEAR -> {
                gradientContainer.visibility = View.VISIBLE
                tabLinear.setTextColor(activeColor)
            }
            GradientType.RADIAL -> {
                gradientContainer.visibility = View.VISIBLE
                tabRadial.setTextColor(activeColor)
            }
        }
    }

    // 🔥(Linear & Radial) 🔥
    private fun updateGradientPreview(stops: List<GradientStop>, mode: GradientType) {

        val sortedStops = stops.sortedBy { it.position }
        val colors = sortedStops.map { it.color }.toIntArray()

        val drawable = GradientDrawable()
        drawable.colors = colors

        if (mode == GradientType.RADIAL) {
            drawable.gradientType = GradientDrawable.RADIAL_GRADIENT

            val radius = if (viewPreview.width > 0) viewPreview.width / 2.5f else 200f
            drawable.gradientRadius = radius

            drawable.setGradientCenter(0.5f, 0.5f)
        } else {
            // Linear Gradient
            drawable.gradientType = GradientDrawable.LINEAR_GRADIENT
            drawable.orientation = GradientDrawable.Orientation.LEFT_RIGHT
        }

        viewPreview.background = drawable
    }

    private fun setupTouchFocusHandling(vararg views: View) {
        val touchListener = View.OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (etHexCode.hasFocus()) {
                    etHexCode.clearFocus()
                    hideKeyboard(v)
                }
            }
            false
        }
        views.forEach { it.setOnTouchListener(touchListener) }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}