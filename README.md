# 🎨 KreateColorPicker

[![](https://jitpack.io/v/khor7d/KreateColorPicker.svg)](https://jitpack.io/#khor7d/KreateColorPicker)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)

**KreateColorPicker** is a highly customizable, modular, and performance-optimized Color Picker library for Android.
Unlike traditional color pickers that force a specific UI dialog, **KreateColorPicker** provides individual views (Hue, Saturation, Alpha, Gradient Bar) and a powerful Logic Controller. This gives you **100% freedom** to design your own UI (XML) while we handle the complex color logic.

## ✨ Features

*   🌈 **Solid Color Mode:** Supports Hue, Saturation, Value, and Alpha transparency.
*   🔄 **Gradient Mode:** Supports **Linear** and **Radial** gradients.
*   🎚 **Gradient Seek Bar:** Add, remove, and move gradient stops easily.
*   ⌨️ **Bi-directional Sync:** Seamless synchronization between Sliders and Hex Input.
*   🎨 **Custom UI:** You design the XML, we handle the logic.
*   ⚡ **High Performance:** Optimized drawing logic for smooth interaction.

---

## 🛠 Installation

### Step 1. Add the JitPack repository
Add this to your project-level `settings.gradle.kts` (or `build.gradle`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = java.net.URI("https://jitpack.io") } // Add this line
    }
}
```

### Step 2. Add the dependency
Add this to your app-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.khor7d:KreateColorPicker:v1.0.0")
}
```

---

## 🚀 Usage Guide

### 1. XML Layout
Design your UI using the library's custom views. You can place them anywhere!

```xml
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="#101622">

    <!-- Preview View -->
    <View
        android:id="@+id/viewPreview"
        android:layout_width="match_parent"
        android:layout_height="80dp"
        android:background="#FFFFFF"
        android:layout_marginBottom="20dp"/>

    <!-- Gradient Slider (Visible only in Gradient Mode) -->
    <com.kpixel.kreatecolorpicker.views.GradientSeekBar
        android:id="@+id/gradientSeekBar"
        android:layout_width="match_parent"
        android:layout_height="40dp"
        android:layout_marginBottom="10dp"
        android:visibility="gone"/>

    <!-- Saturation & Value Map -->
    <com.kpixel.kreatecolorpicker.views.SaturationValueView
        android:id="@+id/saturationView"
        android:layout_width="match_parent"
        android:layout_height="200dp"
        android:layout_marginBottom="20dp"/>

    <!-- Hue Slider -->
    <com.kpixel.kreatecolorpicker.views.HueSliderView
        android:id="@+id/hueSlider"
        android:layout_width="match_parent"
        android:layout_height="30dp"
        android:layout_marginBottom="10dp"/>

    <!-- Alpha Slider -->
    <com.kpixel.kreatecolorpicker.views.AlphaSliderView
        android:id="@+id/alphaSlider"
        android:layout_width="match_parent"
        android:layout_height="30dp"/>

</LinearLayout>
```

### 2. Kotlin Implementation
Connect the views using `KreateColorPicker` controller.

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var colorPicker: KreateColorPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Find Views
        val satView = findViewById<SaturationValueView>(R.id.saturationView)
        val hueSlider = findViewById<HueSliderView>(R.id.hueSlider)
        val alphaSlider = findViewById<AlphaSliderView>(R.id.alphaSlider)
        val gradientSeekBar = findViewById<GradientSeekBar>(R.id.gradientSeekBar)
        val viewPreview = findViewById<View>(R.id.viewPreview)

        // 2. Initialize Library
        colorPicker = KreateColorPicker(satView, hueSlider, alphaSlider, gradientSeekBar)

        // 3. Listen for Solid Color Changes
        colorPicker.onColorChanged = { color, hex ->
            if (colorPicker.activeMode == GradientType.SOLID) {
                viewPreview.setBackgroundColor(color)
            }
            println("Hex Code: #$hex")
        }

        // 4. Listen for Gradient Changes
        colorPicker.onGradientChanged = { stops ->
            // Use 'stops' list to draw Linear/Radial gradient
            // Example: updateGradientPreview(stops)
        }

        // 5. Set Initial Color
        satView.post {
            colorPicker.setColor(Color.RED)
        }
    }
}
```

---

## 🎛 Advanced Features

### Switching Modes (Solid / Linear / Radial)
You can easily switch modes programmatically.

```kotlin
// Switch to Linear Gradient
colorPicker.setMode(GradientType.LINEAR)
gradientSeekBar.visibility = View.VISIBLE

// Switch to Solid
colorPicker.setMode(GradientType.SOLID)
gradientSeekBar.visibility = View.GONE
```

### Delete Gradient Stop
To remove a selected color stop from the gradient bar:

```kotlin
btnDelete.setOnClickListener {
    colorPicker.deleteSelectedStop()
}
```

### Hex Input Sync (Best Practice)
To sync an `EditText` with the color picker without infinite loops, handle focus properly:

```kotlin
// 1. Update EditText from Picker
colorPicker.onColorChanged = { color, hex ->
    if (!etHexCode.hasFocus()) {
        etHexCode.setText(hex)
    }
}

// 2. Update Picker from EditText
etHexCode.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
        if (etHexCode.hasFocus()) {
            try {
                val color = Color.parseColor("#${s.toString()}")
                colorPicker.setColor(color)
            } catch (e: Exception) {}
        }
    }
    // ... other methods
})

// 3. Clear focus when touching sliders (Crucial!)
val touchListener = View.OnTouchListener { v, event ->
    if (event.action == MotionEvent.ACTION_DOWN) {
        if (etHexCode.hasFocus()) {
            etHexCode.clearFocus()
            // Hide keyboard logic here
        }
    }
    false
}
hueSlider.setOnTouchListener(touchListener)
// Apply to all sliders...
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Developed with ❤️ by [Khor7d](https://github.com/khor7d)**
