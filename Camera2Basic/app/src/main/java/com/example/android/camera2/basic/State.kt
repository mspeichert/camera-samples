package com.example.android.camera2.basic

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.log10

class ColorResult(var red: Int, var green: Int, var blue: Int) {}
fun getHue(c: ColorResult): Float {
    val res = Color.rgb(c.red, c.green, c.blue)
    var hsv = FloatArray(3)
    Color.colorToHSV(res, hsv)
    return hsv[0]
}

abstract class CalculationMethod(val ISO: Int, val text: String) {
    abstract fun calculate(a: Double, b: Double, value: ColorResult): Double
}

class JaffeMethod():  CalculationMethod(100, "* measured +") {
    override fun calculate(a: Double, b: Double, value: ColorResult): Double {
        return a * log10(getHue(value).toDouble()) + b
    }
}

class DNBAMethod():  CalculationMethod(200, "* log(measured) +") {
    override fun calculate(a: Double, b: Double, value: ColorResult): Double {
        return a * value.green + b
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