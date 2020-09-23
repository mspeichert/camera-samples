package com.example.android.camera2.basic

import android.graphics.Bitmap
import kotlin.math.log10

class ColorResult(var red: Int, var green: Int, var blue: Int) {}

abstract class CalculationMethod(val ISO: Int, val text: String) {
    abstract fun calculate(a: Double, b: Double, value: Double): Double
}

class JaffeMethod():  CalculationMethod(100, "* measured +") {
    override fun calculate(a: Double, b: Double, value: Double): Double {
        return a * log10(value) + b
    }
}

class DNBAMethod():  CalculationMethod(200, " * log(measured) + ") {
    override fun calculate(a: Double, b: Double, value: Double): Double {
        return a * value + b
    }
}


enum class CMethod(val value: CalculationMethod) {
    Jaffe(JaffeMethod()),
    DNBA(DNBAMethod())
}

object State {
    var colors: ColorResult? = null
    var photo: Bitmap? = null
    var method: CMethod = CMethod.DNBA
    var a: Double? = null
    var b: Double? = null
}