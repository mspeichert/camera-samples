package com.example.android.camera2.basic

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.core.graphics.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt

typealias PixelsByXY = MutableMap<String, PixelData>

class PixelData(var x: Int, var y: Int, var color: Int) {}
class Rect(var top: Int, var left: Int, var right: Int, var bottom: Int) {}

fun displayDouble(d: Double): String {
    return String.format("%.1f", d)
}
fun displayDouble(d: Float): String {
    return String.format("%.1f", d)
}

class Calculations {
    companion object Utils {
        fun xy(x: Int, y: Int): String {
            return x.toString() + "x" + y
        }

        fun rang(x: Int): Array<Int> {
            return Array(x) { i -> i }
        }

        fun areGroupsMergable(g1: PixelsByXY, g2: PixelsByXY): Boolean {
            for ((k, v) in g2) {
                if (findNeighbours(g1, v) { c, n -> compareColors(c.color, n.color, false) < 50 }) return true
            }
            return false
        }

        fun getRGB(b: Bitmap): ColorResult? {

            val colorGroups: MutableList<PixelsByXY> = ArrayList()
            val black = mostPopular(b)

            for (x in 0 until b.width) {
                for (y in 0 until b.height) {
                    val data = PixelData(x, y, b.getPixel(x, y))

                    if (compareColors(data.color, black, false) > 20) {

                        val matchingGroup = colorGroups.find { g -> findNeighbours(g, data) { c, n -> compareColors(c.color, n.color, false) < 50 } }
                        if (matchingGroup == null) colorGroups.add(mutableMapOf(xy(x, y) to data))
                        else matchingGroup.put(xy(x, y), data)
                    }
                }
            }

            var iterator = 1
            while (iterator < colorGroups.size) {
                val r = rang(iterator)
                val mergable = r.find { i -> areGroupsMergable(colorGroups[i], colorGroups[iterator]) }
                if (mergable == null) {
                    iterator++
                    continue
                }
                colorGroups[mergable].putAll(colorGroups[iterator])
                colorGroups.removeAt(iterator)
                iterator = 1
            }
            val mutableB = b.copy(b.config, true)
            colorGroups.forEach {
                paintBitmap(mutableB, findRect(it), Color.RED)
            }
            State.photo = mutableB
            var biggest: PixelsByXY? = null
            for (cg in colorGroups) {
                if (biggest === null) {
                    biggest = cg
                    continue
                }
                if (biggest.size < cg.size) biggest = cg
            }
            val x = 5/2.toDouble()
            if (biggest === null) return null
            val finalRect = cutRect(findRect(biggest), 50)
            val finalGroup = biggest.filter { pd -> isInOval(finalRect, pd.value) }

            val avgRed = finalGroup.map { d -> Color.red(d.value.color).toDouble() }.reduce { a, d -> a+d }/finalGroup.size
            val avgGreen = finalGroup.map { d -> Color.green(d.value.color).toDouble() }.reduce { a,d -> a+d }/finalGroup.size
            val avgBlue = finalGroup.map { d -> Color.blue(d.value.color).toDouble() }.reduce { a,d -> a+d }/finalGroup.size

            return ColorResult(avgRed.toInt(), avgGreen.toInt(), avgBlue.toInt())
        }

        fun squaredDelta(v1: Int, v2: Int): Double {
            return (v1 - v2).toDouble().pow(2)
        }

        fun isInOval(r: Rect, p: PixelData): Boolean {
            val xRadius = (r.right - r.left)/2.toDouble()
            val yRadius = (r.bottom - r.top)/2.toDouble()
            val middleX = r.left + xRadius
            val middleY = r.top + yRadius

            val a = (p.x - middleX)/xRadius
            val b = (p.y - middleY)/yRadius
            val r = a.pow(2) + b.pow(2)
            return r <= 1
        }

        fun cutRect(r: Rect, perc: Int): Rect {
            val mult = (100 - perc) / 100.toDouble()
            val vl = r.bottom - r.top
            val hl = r.right - r.left
            val vdiff = ((vl - vl * mult)/2).toInt()
            val hdiff = ((hl - hl * mult)/2).toInt()
            return Rect(r.top + vdiff, r.left + hdiff, r.right - hdiff, r.bottom - vdiff)
        }


        fun findNeighbours(g: PixelsByXY, c: PixelData, cond: (c: PixelData, n: PixelData) -> Boolean): Boolean {
            for (x in -1 until 2) {
                for (y in -1 until 2) {
                    val neighbour = g[xy(c.x + x, c.y + y)]
                    if (neighbour == null) continue
                    if (cond(c, neighbour)) return true
                }
            }
            return false
        }

        fun paintBitmap(b: Bitmap, r: Rect, color: Int) {
            for (x in r.left until r.right) {
                b[x, r.top] = color
                b[x, r.bottom] = color
            }
            for (y in r.top until r.bottom) {
                b[r.left, y] = color
                b[r.right, y] = color
            }

        }

        fun findRect(g: PixelsByXY): Rect {
            var left = Int.MAX_VALUE
            var right = 0
            var top = Int.MAX_VALUE
            var bottom = 0
            for ((k, p) in g) {
                if (p.x > right) right = p.x
                if (p.x < left) left = p.x
                if (p.y < top) top = p.y
                if (p.y > bottom) bottom = p.y
            }
            return Rect(top, left, right, bottom)
        }

        val conversionIndex = 19.5075

        fun compareColors(c1: Int, c2: Int, log: Boolean): Double {
            if (c1 == c2) return 0.0

            var sum = 0.0
            sum += squaredDelta(Color.red(c1), Color.red(c2))
            sum += squaredDelta(Color.green(c1), Color.green(c2))
            sum += squaredDelta(Color.blue(c1), Color.blue(c2))
            if (log) {
                val red1 = Color.red(c1)
                val green1 = Color.green(c1)
                val blue1 = Color.blue(c1)
                val red2 = Color.red(c2)
                val green2 = Color.green(c2)
                val blue2 = Color.blue(c2)
                val result = sqrt(sum / conversionIndex)
                Log.d("color", "$red1 $red2 $green1 $green2 $blue1 $blue2 $result")
            }


            return sqrt(sum / conversionIndex)
        }

        fun mostPopular(b: Bitmap): Int {
            val repetitions = mutableMapOf<Int, Int>()
            for (x in 0 until b.width) {
                for (y in 0 until b.height) {
                    val p = b.getPixel(x, y)
                    repetitions[p] = repetitions[p]?.plus(1) ?: 1
                }
            }
            var most = repetitions[0] ?: 0
            for ((k, v) in repetitions) {
                if (v > repetitions[most] ?: 0) most = k
            }
            return most
        }
    }
}