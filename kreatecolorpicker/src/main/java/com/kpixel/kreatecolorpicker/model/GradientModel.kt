package com.kpixel.kreatecolorpicker.model

import android.graphics.Color

// গ্রেডিয়েন্টের একটি পয়েন্ট (রঙ এবং অবস্থান)
data class GradientStop(
    var color: Int,
    var position: Float // 0.0 থেকে 1.0
)

// গ্রেডিয়েন্ট টাইপ (Linear, Radial)
enum class GradientType {
    SOLID, LINEAR, RADIAL
}