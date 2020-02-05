package com.bendenen.visionai.tflite.styletransfer.utils

import android.graphics.Bitmap
import android.graphics.Color

@Throws(IllegalArgumentException::class)
internal fun Array<Array<Array<FloatArray>>>.toBitmap(): Bitmap {
    require(this.size == 1) { "The first dimension of Array is too big ${this.size}" }
    val bitmapContentArray = this[0]
    val width = bitmapContentArray.size
    val height = bitmapContentArray[0].size
    val channels = bitmapContentArray[0][0].size
    require(channels == 3) { "Channels should be equals 3 for RGB image but it is $channels" }
    val pixelValues = IntArray(width * height)
    var index = 0
    for (w in 0 until width) {
        for (h in 0 until height) {
            val r = (bitmapContentArray[w][h][0] * 255).toInt()
            val g = (bitmapContentArray[w][h][1] * 255).toInt()
            val b = (bitmapContentArray[w][h][2] * 255).toInt()
            pixelValues[index++] = Color.rgb(r, g, b)
        }
    }
    return Bitmap.createBitmap(pixelValues, width, height, Bitmap.Config.ARGB_8888)
}