package com.example.android.camera2.basic

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import kotlin.math.log10
import kotlin.math.pow

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

class JaffeMethod():  CalculationMethod(100, "* log(crea) +") {
    override fun calculate(a: Double, b: Double, value: ColorResult): Double {
        val pow = (getHue(value).toDouble() - b) / a
        return 10.0.pow(pow)
    }
}

class DNBAMethod():  CalculationMethod(200, "* crea +") {
    override fun calculate(a: Double, b: Double, value: ColorResult): Double {
        return (value.green - b) / a
    }
}


enum class CMethod(val value: CalculationMethod) {
    Jaffe(JaffeMethod()),
    DNBA(DNBAMethod())
}

object State {
    var colors: ColorResult? = null
    var photo: Bitmap? = null
    var cameraId: String? = null
    var imageFormat = ImageFormat.JPEG
    var method: CMethod = CMethod.DNBA
    var a: Double? = null
    var b: Double? = null
}